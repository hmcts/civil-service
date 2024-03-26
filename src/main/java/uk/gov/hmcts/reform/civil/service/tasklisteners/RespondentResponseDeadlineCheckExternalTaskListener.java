package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.RespondentResponseDeadlineCheckHandler;

@Component
public class RespondentResponseDeadlineCheckExternalTaskListener {

    private static final String TOPIC = "RESPONDENT_RESPONSE_DEADLINE_CHECK";

    @Autowired
    private RespondentResponseDeadlineCheckExternalTaskListener(
        RespondentResponseDeadlineCheckHandler respondentResponseDeadlineCheckHandler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(respondentResponseDeadlineCheckHandler).open();
    }
}
