package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.RequestForReconsiderationNotificationDeadlineHandler;

@Component
public class RequestForReconsiderationNotificationDeadlineExternalTaskListener {

    private static final String TOPIC = "REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CHECK";

    @Autowired
    private RequestForReconsiderationNotificationDeadlineExternalTaskListener(RequestForReconsiderationNotificationDeadlineHandler handler,
                                                                              ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(handler).open();
    }
}
