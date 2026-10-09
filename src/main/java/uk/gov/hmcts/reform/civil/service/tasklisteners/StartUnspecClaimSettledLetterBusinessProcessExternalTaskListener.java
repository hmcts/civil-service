package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.StartUnspecClaimSettledLetterBusinessProcessTaskHandler;

@Component
public class StartUnspecClaimSettledLetterBusinessProcessExternalTaskListener {

    private static final String TOPIC = "START_UNSPEC_CLAIM_SETTLED_LETTER_BUSINESS_PROCESS";

    @Autowired
    private StartUnspecClaimSettledLetterBusinessProcessExternalTaskListener(
        StartUnspecClaimSettledLetterBusinessProcessTaskHandler taskHandler,
        ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(taskHandler).open();
    }
}
