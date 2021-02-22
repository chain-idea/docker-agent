package com.gzqylc.docker_admin.agent.docker;

import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.model.PushResponseItem;
import com.gzqylc.docker_admin.agent.RemoteLogger;

public class PushImageCallback extends ResultCallbackTemplate<PushImageCallback, PushResponseItem> {

    private RemoteLogger logger;
    private String error;

    public PushImageCallback(RemoteLogger logger) {
        this.logger = logger;
    }

    @Override
    public void onNext(PushResponseItem item) {
        if (item.isErrorIndicated()) {
            this.error = item.getError();
        }
        logger.info(item);
    }

    public String getError() {
        return error;
    }
}
