package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.ClaimDetailsNotificationDeadlineHandler;

@Component
public class ClaimDetailsNotificationDeadlineExternalTaskListener {

    private static final String TOPIC = "CLAIM_DETAILS_NOTIFICATION_DEADLINE";

    @Autowired
    ClaimDetailsNotificationDeadlineExternalTaskListener(ClaimDetailsNotificationDeadlineHandler handler,
                                                        ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(handler).open();
    }
}
