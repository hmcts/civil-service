package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.HmcMessageHandler;

@Component
public class HandleHMCMessageExternalTaskListener {

    private static final String TOPIC = "HANDLE_HMC_MESSAGE_TASK";

    @Autowired
    private HandleHMCMessageExternalTaskListener(HmcMessageHandler hmcMessageHandler,
                                                 ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(hmcMessageHandler).open();
    }
}
