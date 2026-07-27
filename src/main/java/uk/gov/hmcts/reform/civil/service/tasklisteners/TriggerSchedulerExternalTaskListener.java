package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.TriggerSchedulerExternalTaskHandler;

@Component
public class TriggerSchedulerExternalTaskListener {

    private static final String TOPIC = "TRIGGER_SCHEDULER";

    @Autowired
    TriggerSchedulerExternalTaskListener(TriggerSchedulerExternalTaskHandler handler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(handler).open();
    }
}
