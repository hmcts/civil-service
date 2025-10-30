package uk.gov.hmcts.reform.civil.ga.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.handler.tasks.GaStartGeneralApplicationBusinessProcessTaskHandler;

@Component
public class StartGeneralApplicationBusinessProcessExternalTaskListener {

    private static final String TOPIC = "START_GA_BUSINESS_PROCESS";

    @Autowired
    private StartGeneralApplicationBusinessProcessExternalTaskListener(
            GaStartGeneralApplicationBusinessProcessTaskHandler startGeneralApplicationBusinessProcessTaskHandler,
            ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(startGeneralApplicationBusinessProcessTaskHandler).open();
    }
}
