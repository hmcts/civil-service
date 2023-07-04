package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.StartHearingNoticeBusinessProcessTaskHandler;

@Component
public class StartHearingNoticeBusinessProcessExternalTaskListener {

    private static final String TOPIC = "START_HEARING_NOTICE_BUSINESS_PROCESS";

    @Autowired
    private StartHearingNoticeBusinessProcessExternalTaskListener(StartHearingNoticeBusinessProcessTaskHandler startBusinessProcessTaskHandler,
                                                                  ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(startBusinessProcessTaskHandler).open();
    }
}
