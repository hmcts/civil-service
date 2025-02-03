package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.CoscApplicationProcessorHandler;

@Component
public class CoscApplicationProcessorExternalTaskListener {

    private static final String TOPIC = "CoscApplicationProcessor";

    @Autowired
    private CoscApplicationProcessorExternalTaskListener(CoscApplicationProcessorHandler handler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(handler).open();
    }
}
