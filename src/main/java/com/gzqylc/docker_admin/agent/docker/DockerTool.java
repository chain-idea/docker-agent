package com.gzqylc.docker_admin.agent.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DockerTool {


    public static DockerClient getClient() {
        return getClient(null, null, null);
    }

    public static DockerClient getClient(String registryUrl, String registryUsername, String registryPassword) {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withRegistryUsername(registryUsername)
                .withRegistryPassword(registryPassword)
                .withRegistryUrl(registryUrl)
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();
        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
        return dockerClient;


    }


}
