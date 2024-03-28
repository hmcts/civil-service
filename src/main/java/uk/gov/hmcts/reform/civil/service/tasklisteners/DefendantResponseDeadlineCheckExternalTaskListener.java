package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.DefendantResponseDeadlineCheckHandler;

@Component
public class DefendantResponseDeadlineCheckExternalTaskListener {

    private static final String TOPIC = "DEFENDANT_RESPONSE_DEADLINE_CHECK";

    @Autowired
    private DefendantResponseDeadlineCheckExternalTaskListener(
        DefendantResponseDeadlineCheckHandler defendantResponseDeadlineCheckHandler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(defendantResponseDeadlineCheckHandler).open();
    }
}
