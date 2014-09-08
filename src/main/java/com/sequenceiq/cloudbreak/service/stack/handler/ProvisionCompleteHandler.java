package com.sequenceiq.cloudbreak.service.stack.handler;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reactor.event.Event;
import reactor.function.Consumer;

import com.sequenceiq.cloudbreak.conf.ReactorConfig;
import com.sequenceiq.cloudbreak.domain.CloudPlatform;
import com.sequenceiq.cloudbreak.domain.Resource;
import com.sequenceiq.cloudbreak.repository.RetryingStackUpdater;
import com.sequenceiq.cloudbreak.service.stack.event.ProvisionComplete;
import com.sequenceiq.cloudbreak.service.stack.flow.MetadataSetupContext;

@Component
public class ProvisionCompleteHandler implements Consumer<Event<ProvisionComplete>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProvisionCompleteHandler.class);

    @Autowired
    private MetadataSetupContext metadataSetupContext;

    @Autowired
    private RetryingStackUpdater retryingStackUpdater;

    @Override
    public void accept(Event<ProvisionComplete> event) {
        ProvisionComplete stackCreateComplete = event.getData();
        CloudPlatform cloudPlatform = stackCreateComplete.getCloudPlatform();
        Long stackId = stackCreateComplete.getStackId();
        LOGGER.info("Accepted {} event on stack '{}'.", ReactorConfig.PROVISION_COMPLETE_EVENT, stackId);
        Set<Resource> resourcesSet = event.getData().getResources();
        retryingStackUpdater.updateStackResources(stackId, resourcesSet);
        metadataSetupContext.setupMetadata(cloudPlatform, stackId);
    }

}