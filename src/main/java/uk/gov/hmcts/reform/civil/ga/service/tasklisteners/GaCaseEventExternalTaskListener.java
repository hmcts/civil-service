package uk.gov.hmcts.reform.civil.ga.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.handler.tasks.GaCaseEventTaskHandler;

@Component
public class GaCaseEventExternalTaskListener {

    private static final String TOPIC = "processCaseEventGASpec";

    @Autowired
    private GaCaseEventExternalTaskListener(GaCaseEventTaskHandler caseEventTaskHandler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(caseEventTaskHandler).open();
    }
}
