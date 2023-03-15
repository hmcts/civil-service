package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.TrialReadyNotificationCheckHandler;

@Component
public class TrialReadyNotificationExternalTaskListener {

    private static final String TOPIC = "TRIAL_READY_NOTIFICATION_CHECK";

    @Autowired
    private TrialReadyNotificationExternalTaskListener(TrialReadyNotificationCheckHandler handler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(handler).open();
    }
}
