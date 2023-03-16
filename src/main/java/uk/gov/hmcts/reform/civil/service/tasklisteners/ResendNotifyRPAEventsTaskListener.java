package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.ResendNotifyRPAEventsHandler;

@Component
public class ResendNotifyRPAEventsTaskListener {

    private static final String TOPIC = "RESEND_NOTIFY_RPA_EVENTS";

    @Autowired
    private ResendNotifyRPAEventsTaskListener(ResendNotifyRPAEventsHandler resendNotifyRPAEventsHandler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(resendNotifyRPAEventsHandler).open();
    }
}
