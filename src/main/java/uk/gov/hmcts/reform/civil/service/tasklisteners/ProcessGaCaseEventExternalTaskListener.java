package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.ProcessGaCaseEventTaskHandler;

@Component
public class ProcessGaCaseEventExternalTaskListener {

    private static final String TOPIC = "processGaCaseEvent";

    @Autowired
    private ProcessGaCaseEventExternalTaskListener(ProcessGaCaseEventTaskHandler processGaCaseEventHandler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(processGaCaseEventHandler).open();
    }
}
