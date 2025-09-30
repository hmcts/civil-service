package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.CreateApplicationTaskHandler;

@Component
public class CreateApplicationExternalTaskListener {

    private static final String TOPIC = "createApplicationEventGASpec";

    @Autowired
    private CreateApplicationExternalTaskListener(CreateApplicationTaskHandler createApplicationTaskHandler,
                                                  ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(createApplicationTaskHandler).open();
    }
}
