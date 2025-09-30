package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.StartGeneralApplicationBusinessProcessTaskHandler;

@Component
public class StartHearingScheduledEventExternalTaskListener {

    private static final String TOPIC = "START_HEARING_SCHEDULED_BUSINESS_PROCESS";

    @Autowired
    private StartHearingScheduledEventExternalTaskListener(
        StartGeneralApplicationBusinessProcessTaskHandler startGeneralApplicationBusinessProcessTaskHandler,
        ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(startGeneralApplicationBusinessProcessTaskHandler).open();
    }
}
