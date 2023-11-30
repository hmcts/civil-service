package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.RetriggerCasesEventHandler;

@Component
public class RetriggerCasesTaskListener {

    private static final String TOPIC = "RETRIGGER_CASES_EVENTS";

    @Autowired
    private RetriggerCasesTaskListener(RetriggerCasesEventHandler retriggerCasesEventHandler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(retriggerCasesEventHandler).open();
    }
}
