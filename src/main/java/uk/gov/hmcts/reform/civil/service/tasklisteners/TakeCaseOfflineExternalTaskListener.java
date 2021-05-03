package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.TakeCaseOfflineHandler;

@Component
public class TakeCaseOfflineExternalTaskListener {

    private static final String TOPIC = "TAKE_CASE_OFFLINE";

    @Autowired
    private TakeCaseOfflineExternalTaskListener(TakeCaseOfflineHandler handler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(handler).open();
    }
}
