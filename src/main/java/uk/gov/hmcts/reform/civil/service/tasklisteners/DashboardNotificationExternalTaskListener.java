package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.DashboardNotificationTaskHandler;

import static uk.gov.hmcts.reform.civil.handler.tasks.DashboardNotificationTaskHandler.CIVIL_TOPIC;
import static uk.gov.hmcts.reform.civil.handler.tasks.DashboardNotificationTaskHandler.GA_TOPIC;

@Component
public class DashboardNotificationExternalTaskListener {

    @Autowired
    private DashboardNotificationExternalTaskListener(DashboardNotificationTaskHandler handler, ExternalTaskClient client) {
        subscribe(client, CIVIL_TOPIC, handler);
        subscribe(client, GA_TOPIC, handler);
    }

    private void subscribe(ExternalTaskClient client, String topic, DashboardNotificationTaskHandler handler) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(topic);
        subscriptionBuilder.handler(handler).open();
    }
}
