package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.BundleCreationTriggerHandler;

@Component
public class BundleCreationTriggerHandlerExternalTaskListener {

    private static final String TOPIC = "BUNDLE_CREATION_CHECK";

    @Autowired
    private BundleCreationTriggerHandlerExternalTaskListener(BundleCreationTriggerHandler  bundleCreationTriggerHandler,
                                                           ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC).lockDuration(600000);
        subscriptionBuilder.handler(bundleCreationTriggerHandler).open();
    }
}
