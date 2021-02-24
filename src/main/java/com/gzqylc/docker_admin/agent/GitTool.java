package com.gzqylc.docker_admin.agent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;

@Slf4j
public class GitTool {


    public static void clone(String url, String user, String password, String value, File workDir) throws GitAPIException {
        long start = System.currentTimeMillis();
        log.info("工作目录为 {}", workDir.getAbsolutePath());

        log.info("克隆的分支、标签、commit {}", value);
        boolean isCommitId = value.length() == 40;

        log.info("是否commitId {}", isCommitId);


        log.info("获取代码 git clone {}", url);
        UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(user, password);

        if (workDir.exists() && workDir.listFiles().length > 0) {
            workDir.delete();
        }

        CloneCommand cloneCommand = Git.cloneRepository().setURI(url).setCredentialsProvider(provider).setDirectory(workDir);
        cloneCommand.setCloneSubmodules(true);
        if (!isCommitId) {
            cloneCommand.setBranch(value);
        }
        Git git = cloneCommand.call();

        if (isCommitId) {
            log.info("resetRef {}", value);
            git.reset().setRef(value).setMode(ResetCommand.ResetType.HARD).call();
        }

        git.close();

        log.info("文件列表:");
        for (String f : workDir.list()) {
            log.info(f);
        }

        log.info("代码获取完毕, 共 {} M", FileUtils.sizeOfDirectory(workDir) / 1024 / 1024);
        log.info("耗时：{} 秒", (System.currentTimeMillis() - start) / 1000);
    }

    public static void main(String[] args) throws GitAPIException {
        clone("http://www.gzqylc.com:7000/jiangtao/test2.git",
                "jiangtao",
                "kimi_kissme",
                "a0266e7de165d2aa600e74e62036bb4c6dbf82ba",
                new File("d:/test2/fb"));
    }

}
