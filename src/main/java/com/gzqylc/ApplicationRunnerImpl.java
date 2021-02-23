package com.gzqylc;

import com.gzqylc.docker_admin.agent.docker.DockerTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.File;

/**
 * 启动后执行
 * @author 姜涛
 */
@Component
@Slf4j
public class ApplicationRunnerImpl implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        DockerTool.getClient().pingCmd().exec();

        String os = System.getProperty("os.name").toLowerCase();
        log.info("操作系统为:{}", os);
        if (os.contains("linux")) {
            File dockerSock = new File("/var/run/docker.sock");

            Assert.state(dockerSock.exists(), "文件" + dockerSock.getPath() + "不存在,如果本软件运行于docker中，请映射该文件");
        }
    }
}
