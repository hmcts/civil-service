package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.StartGeneralApplicationBusinessProcessTaskHandler;

@Component
public class StartGeneralApplicationBusinessProcessExternalTaskListener {

    private static final String TOPIC = "START_BUSINESS_PROCESS_GASPEC";

    @SuppressWarnings("checkstyle:LineLength")
    @Autowired
    private StartGeneralApplicationBusinessProcessExternalTaskListener(
            StartGeneralApplicationBusinessProcessTaskHandler startGeneralApplicationBusinessProcessTaskHandler,
            ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(startGeneralApplicationBusinessProcessTaskHandler).open();
    }
}
