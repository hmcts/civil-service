package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.AutomatedHearingNoticeHandler;

@Component
public class HearingNoticeSchedulerExternalTaskListener {

    private static final String TOPIC = "AUTOMATED_HEARING_NOTICE";

    @Autowired
    private HearingNoticeSchedulerExternalTaskListener(AutomatedHearingNoticeHandler automatedHearingNoticeEventEmitterHandler,
                                                       ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(automatedHearingNoticeEventEmitterHandler).open();
    }
}
