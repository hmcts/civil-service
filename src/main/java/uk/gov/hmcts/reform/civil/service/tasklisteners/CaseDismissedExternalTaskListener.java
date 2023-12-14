package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.ClaimDismissedHandler;

@Component
public class CaseDismissedExternalTaskListener {

    private static final String TOPIC = "CASE_DISMISSED";

    @Autowired
    private CaseDismissedExternalTaskListener(ClaimDismissedHandler claimDismissedHandler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(claimDismissedHandler).open();
    }
}
