package com.gzqylc.docker_admin.agent;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequestMapping("agent")
@RestController
@Slf4j
public class BuildImageController {


    @Autowired
    BuildService buildService;

    @RequestMapping
    public String index() {
        return "docker-admin-agent";
    }




    @RequestMapping("build")
    public String buildImage(@RequestBody BuildImageForm form) throws GitAPIException, IOException, InterruptedException {
        buildService.buildImage(form);
        return "正在执行命令";
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
