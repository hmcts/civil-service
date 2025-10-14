package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.GaSpecExternalCaseEventTaskHandler;

@Component
public class GASpecCaseEventExternalTaskListener {

    private static final String TOPIC = "processExternalCaseEventGASpec";

    @Autowired
    private GASpecCaseEventExternalTaskListener(GaSpecExternalCaseEventTaskHandler
                                                    caseEventTaskHandler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(caseEventTaskHandler).open();
    }
}
