package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.EndHearingScheduledBusinessProcessTaskHandler;

@Component
public class EndHearingScheduledEventExternalTaskListener {

    private static final String TOPIC = "END_HEARING_SCHEDULED_PROCESS_GASPEC";

    @Autowired
    private EndHearingScheduledEventExternalTaskListener(
        EndHearingScheduledBusinessProcessTaskHandler endHearingScheduledBusinessProcessTaskHandler,
        ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(endHearingScheduledBusinessProcessTaskHandler).open();
    }
}
