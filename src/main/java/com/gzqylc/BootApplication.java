package com.gzqylc;

import com.gzqylc.docker_admin.agent.docker.DockerTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.Assert;

import java.io.File;

@SpringBootApplication
@EnableAsync
@Slf4j
public class BootApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootApplication.class, args);


        DockerTool.getClient().pingCmd().exec();

        String os = System.getProperty("os.name");
        if (os.contains("linux")) {
            File dockerSock = new File("/var/run/docker.sock");

            Assert.state(dockerSock.exists(), "文件" + dockerSock.getPath() + "不存在,如果本软件运行于docker中，请映射该文件");
        }
    }


}
