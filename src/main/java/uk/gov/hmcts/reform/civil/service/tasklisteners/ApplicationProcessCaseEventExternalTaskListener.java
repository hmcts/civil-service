package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.ApplicationProcessCaseEventTaskHandler;

@Component
public class ApplicationProcessCaseEventExternalTaskListener {

    private static final String TOPIC = "applicationProcessCaseEventGASpec";

    @Autowired
    private ApplicationProcessCaseEventExternalTaskListener(
        ApplicationProcessCaseEventTaskHandler applicationProcessCaseEventTaskHandler,
                                                            ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(applicationProcessCaseEventTaskHandler).open();
    }
}
