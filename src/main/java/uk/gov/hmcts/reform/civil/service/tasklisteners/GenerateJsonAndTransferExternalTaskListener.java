package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.GenerateMediationJsonAndTransferTaskHandler;

@Component
public class GenerateJsonAndTransferExternalTaskListener {

    private static final String TOPIC = "GenerateJsonAndSendToMmt";

    @Autowired
    private GenerateJsonAndTransferExternalTaskListener(GenerateMediationJsonAndTransferTaskHandler taskHandler,
                                                        ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(taskHandler).open();
    }
}
