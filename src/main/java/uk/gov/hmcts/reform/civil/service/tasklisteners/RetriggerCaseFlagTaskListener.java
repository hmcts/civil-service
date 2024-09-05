package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.RetriggerCaseFlagEventHandler;

@Component
public class RetriggerCaseFlagTaskListener {

    private static final String TOPIC = "RETRIGGER_CASE_FLAG_EVENT";

    @Autowired
    private RetriggerCaseFlagTaskListener(RetriggerCaseFlagEventHandler caseFlagEventHandler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(caseFlagEventHandler).open();
    }
}
