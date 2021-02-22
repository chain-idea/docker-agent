
package com.gzqylc.docker_admin.agent.docker;

import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.gzqylc.docker_admin.agent.RemoteLogger;

import java.util.concurrent.TimeUnit;


public class MyBuildImageResultCallback extends ResultCallbackTemplate<MyBuildImageResultCallback, BuildResponseItem> {

    private String imageId;

    private String error;

    private RemoteLogger logger;

    public MyBuildImageResultCallback(RemoteLogger logger) {
        this.logger = logger;
    }

    @Override
    public void onNext(BuildResponseItem item) {
        if (item.isBuildSuccessIndicated()) {
            this.imageId = item.getImageId();
        } else if (item.isErrorIndicated()) {
            this.error = item.getError();
        }

        logger.info(item);


    }


    /**
     * Awaits the image id from the response stream.
     *
     * @throws DockerClientException if the build fails.
     */
    public String awaitImageId() {
        try {
            awaitCompletion();
        } catch (InterruptedException e) {
            throw new DockerClientException("", e);
        }

        return getImageId();
    }

    /**
     * Awaits the image id from the response stream.
     *
     * @throws DockerClientException if the build fails or the timeout occurs.
     */
    public String awaitImageId(long timeout, TimeUnit timeUnit) {
        try {
            awaitCompletion(timeout, timeUnit);
        } catch (InterruptedException e) {
            throw new DockerClientException("Awaiting image id interrupted: ", e);
        }

        return getImageId();
    }

    private String getImageId() {
        if (imageId != null) {
            return imageId;
        }

        if (error == null) {
            throw new DockerClientException("Could not build image");
        }

        throw new DockerClientException("Could not build image: " + error);
    }
}
