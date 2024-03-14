package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.FullAdmitPayImmediatelyNoPaymentFromDefendantHandler;

@Component
public class FullAdmitPayImmediatelyNoPaymentFromDefendantExternalTaskListener {

    private static final String TOPIC = "FULL_ADMIT_PAY_IMMEDIATELY_NO_PAYMENT_CHECK";

    @Autowired
    private FullAdmitPayImmediatelyNoPaymentFromDefendantExternalTaskListener(
        FullAdmitPayImmediatelyNoPaymentFromDefendantHandler fullAdmitPayImmediatelyNoPaymentFromDefendantHandler,
        ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(fullAdmitPayImmediatelyNoPaymentFromDefendantHandler).open();
    }
}
