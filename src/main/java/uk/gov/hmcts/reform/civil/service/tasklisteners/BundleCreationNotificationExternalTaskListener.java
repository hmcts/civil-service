package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.BundleCreationTriggerHandler;

@Component
public class BundleCreationNotificationExternalTaskListener {

    private static final String TOPIC = "BUNDLE_CREATION_NOTIFICATION_CHECK";

    @Autowired
    private BundleCreationNotificationExternalTaskListener(BundleCreationTriggerHandler handler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(handler).open();
    }
}
