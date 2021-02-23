package com.gzqylc.docker_admin.agent;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.PushImageCmd;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.Sets;
import com.gzqylc.docker_admin.agent.docker.DockerTool;
import com.gzqylc.docker_admin.agent.docker.MyBuildImageResultCallback;
import com.gzqylc.docker_admin.agent.docker.MyPushImageCallback;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
public class BuildService {


    @Async
    public void buildImage(BuildImageController.BuildImageForm form) {
        RemoteLogger log = RemoteLogger.getLogger(form.getLogHook());
        try {


            log.info("开始构建镜像任务开始");

            // 获取代码
            File workDir = new File("/tmp/" + UUID.randomUUID());
            log.info("工作目录为 {}", workDir.getAbsolutePath());
            log.info("获取代码 git clone {}", form.gitUrl);

            UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(form.gitUsername, form.gitPassword);

            if (workDir.exists()) {
                boolean delete = workDir.delete();
                Assert.state(delete, "删除文件失败");
            }

            Git git = Git.cloneRepository()
                    .setURI(form.gitUrl)
                    .setNoTags()
                    .setCredentialsProvider(provider)
                    .setDirectory(workDir)
                    .call();

            String commitMsg = git.log().call().iterator().next().getFullMessage();
            log.info("git log : {}", commitMsg);
            git.close();


            log.info("代码获取完毕, 共 {} M", FileUtils.sizeOfDirectory(workDir) / 1024 / 1024);

            log.info("连接构建主机容器引擎中...");
            DockerClient dockerClient = DockerTool.getClient(form.regHost, form.regUsername, form.regPassword);
            String latestTag = form.imageUrl + ":latest";
            String commitTag = form.imageUrl + ":" + form.branch;
            Set<String> tags = Sets.newHashSet(latestTag, commitTag);


            File buildDir = new File(workDir, form.buildContext);


            BuildImageCmd buildImageCmd = dockerClient.buildImageCmd(buildDir).withTags(tags);
            buildImageCmd.withNoCache(false);

            log.info("向docker发送构建指令");
            MyBuildImageResultCallback buildCallback = new MyBuildImageResultCallback(log);
            String imageId = buildImageCmd.exec(buildCallback).awaitImageId();
            log.info("镜像构建结束 imageId={}", imageId);

            // 推送
            log.info("推送镜像");
            for (String tag : tags) {
                PushImageCmd pushImageCmd = dockerClient.pushImageCmd(tag);

                pushImageCmd.exec(new MyPushImageCallback(log)).awaitCompletion();
            }

            dockerClient.close();


            log.info("构建阶段结束");

            HttpRequest.get(form.getResultHook() + "/true").body();


        } catch (Exception e) {
            log.info("构建异常 {}", e.getMessage());
            HttpRequest.get(form.getResultHook() + "/false").body();
        }
    }
}
