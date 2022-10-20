package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.HearingFeeDueHandler;

@Component
public class HearingFeeDueExternalTaskListener {

    private static final String TOPIC = "HEARING_FEE_CHECK";

    @Autowired
    private HearingFeeDueExternalTaskListener(HearingFeeDueHandler handler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(handler).open();
    }
}
