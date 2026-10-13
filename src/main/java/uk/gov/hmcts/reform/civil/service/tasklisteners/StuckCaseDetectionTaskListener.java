package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.StuckCaseDetectionEventHandler;

@Component
public class StuckCaseDetectionTaskListener {

    private static final String TOPIC = "STUCK_CASE_DETECTION_EVENT";

    @Autowired
    StuckCaseDetectionTaskListener(StuckCaseDetectionEventHandler stuckCaseDetectionEventHandler,
                              ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(stuckCaseDetectionEventHandler).open();
    }
}
