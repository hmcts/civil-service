package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.GeneralApplicationTaskHandler;

@Component
public class GeneralApplicationEventExternalTaskListener {

    private static final String TOPIC = "applicationEventGASpec";

    @Autowired
    private GeneralApplicationEventExternalTaskListener(GeneralApplicationTaskHandler generalApplicationTaskHandler,
                                                        ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(generalApplicationTaskHandler).open();
    }
}
