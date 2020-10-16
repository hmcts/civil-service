package uk.gov.hmcts.reform.unspec.service.tasks.listener;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.unspec.service.tasks.handler.PollingEventEmitterHandler;

@Component
public class PollingEventEmitterExternalTaskListener {

    private static final String TOPIC = "POLLING_EVENT_EMITTER";

    @Autowired
    private PollingEventEmitterExternalTaskListener(PollingEventEmitterHandler pollingEventEmitterHandler,
                                                    ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(pollingEventEmitterHandler).open();
    }
}
