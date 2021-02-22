package com.gzqylc.docker_admin.agent;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.PushImageCmd;
import com.google.common.collect.Sets;
import com.gzqylc.docker_admin.agent.docker.DockerTool;
import com.gzqylc.docker_admin.agent.docker.MyBuildImageResultCallback;
import com.gzqylc.docker_admin.agent.docker.MyPushImageCallback;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@RequestMapping("agent")
@RestController
@Slf4j
public class BuildImageController {

    @RequestMapping
    public String index() {
        return "docker-admin-agent";
    }


    @RequestMapping("build")
    public void buildImage(@RequestBody BuildImageForm form) throws GitAPIException, IOException, InterruptedException {
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
        RemoteLogger logger = RemoteLogger.getLogger(form.getLogUrl());

        BuildImageCmd buildImageCmd = dockerClient.buildImageCmd(buildDir).withTags(tags);
        buildImageCmd.withNoCache(false);

        log.info("向docker发送构建指令");
        String imageId = buildImageCmd.exec(new MyBuildImageResultCallback(logger)).awaitImageId();
        log.info("镜像构建结束 imageId={}", imageId);

        // 推送
        log.info("推送镜像");
        for (String tag : tags) {
            PushImageCmd pushImageCmd = dockerClient.pushImageCmd(tag);

            pushImageCmd.exec(new MyPushImageCallback(logger)).awaitCompletion();
        }

        dockerClient.close();


        log.info("构建阶段结束");
    }


    @Data
    public static class BuildImageForm {
        String gitUrl;
        String gitUsername;
        String gitPassword;
        String branch;
        String regHost;
        String regUsername;
        String regPassword;
        String imageUrl;
        String buildContext;
        String logUrl;
    }
}
