package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.SettlementNoResponseFromDefendantHandler;

@Component
public class SettlementNoResponseFromDefendantExternalTaskListener {

    private static final String TOPIC = "SETTLEMENT_NO_RESPONSE_FROM_DEFENDANT_CHECK";

    @Autowired
    private SettlementNoResponseFromDefendantExternalTaskListener(
        SettlementNoResponseFromDefendantHandler settlementNoResponseFromDefendantHandler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(settlementNoResponseFromDefendantHandler).open();
    }
}
