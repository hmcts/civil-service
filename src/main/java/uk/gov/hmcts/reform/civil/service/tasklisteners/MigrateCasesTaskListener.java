package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.RetriggerCasesEventHandler;

@Component
public class MigrateCasesTaskListener {

    private static final String TOPIC = "MIGRATE_CASES_EVENTS";

    @Autowired
    private MigrateCasesTaskListener(RetriggerCasesEventHandler retriggerCasesEventHandler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(retriggerCasesEventHandler).open();
    }
}
