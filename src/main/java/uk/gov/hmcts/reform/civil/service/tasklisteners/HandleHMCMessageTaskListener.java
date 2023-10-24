package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.civil.handler.HmcMessageHandler;

public class HandleHMCMessageTaskListener {

    private static final String TOPIC = "HANDLE_HMC_MESSAGE_TASK";

    @Autowired
    private HandleHMCMessageTaskListener(HmcMessageHandler hmcMessageHandler,
                                         ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(hmcMessageHandler).open();
    }
}
