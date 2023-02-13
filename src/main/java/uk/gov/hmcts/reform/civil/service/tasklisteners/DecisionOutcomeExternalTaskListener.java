package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.DecisionOutcomeHandler;

@Component
public class DecisionOutcomeExternalTaskListener {

    private static final String TOPIC = "MOVE_TO_DECISION_OUTCOME";

    @Autowired
    private DecisionOutcomeExternalTaskListener(DecisionOutcomeHandler handler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(handler).open();
    }
}
