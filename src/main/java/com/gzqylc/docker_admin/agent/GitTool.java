package com.gzqylc.docker_admin.agent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ReflogEntry;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

@Slf4j
public class GitTool {


    public static void clone(String url, String user, String password, String value, File workDir) throws GitAPIException {
        long start = System.currentTimeMillis();

        boolean isCommitRef = value.length() == 40;

        log.info("工作目录为 {}", workDir.getAbsolutePath());
        log.info("获取代码 git clone {}", url);
        UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(user, password);

        if (workDir.exists()) {
            workDir.delete();
        }

        CloneCommand cloneCommand = Git.cloneRepository().setURI(url).setCredentialsProvider(provider).setDirectory(workDir);

        if (!isCommitRef) {
            cloneCommand.setBranch(value);
        }
        Git git = cloneCommand.call();

        if (isCommitRef) {
            git.reset().setRef(value).call();
        }

        git.close();
        log.info("代码获取完毕, 共 {} M", FileUtils.sizeOfDirectory(workDir) / 1024 / 1024);

        log.info("耗时：{} 秒", (System.currentTimeMillis() - start) / 1000);
    }

}
