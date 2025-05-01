package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.GenerateCsvAndTransferTaskHandler;

@Component
public class GenerateCsvAndTransferExternalTaskListener {

    private static final String TOPIC = "GenerateCsvAndSendToMmt";

    @Autowired
    private GenerateCsvAndTransferExternalTaskListener(GenerateCsvAndTransferTaskHandler taskHandler,
                                                       ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(taskHandler).open();
    }
}
