package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.UpdateFromGACaseEventTaskHandler;

@Component
public class UpdateFromGACaseEventTaskListener {

    private static final String TOPIC = "updateFromGACaseEvent";

    @Autowired
    private UpdateFromGACaseEventTaskListener(UpdateFromGACaseEventTaskHandler updateFromGACaseEventTaskHandler,
                                              ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(updateFromGACaseEventTaskHandler).open();
    }
}
