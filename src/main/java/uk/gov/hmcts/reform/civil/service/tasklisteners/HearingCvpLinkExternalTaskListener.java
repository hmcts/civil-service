package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.CvpJoinLinkSchedulerHandler;

@Component
public class HearingCvpLinkExternalTaskListener {

    private static final String TOPIC = "HEARING_CVP_LINK";

    @Autowired
    private HearingCvpLinkExternalTaskListener(CvpJoinLinkSchedulerHandler cvpLinkHandler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(cvpLinkHandler).open();
    }
}
