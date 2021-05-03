package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.CaseEventTaskHandler;

@Component
public class CaseEventExternalTaskListener {

    private static final String TOPIC = "processCaseEvent";

    @Autowired
    private CaseEventExternalTaskListener(CaseEventTaskHandler caseEventTaskHandler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(caseEventTaskHandler).open();
    }
}
