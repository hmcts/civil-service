package uk.gov.hmcts.reform.civil.ga.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.handler.tasks.GaStartGeneralApplicationBusinessProcessTaskHandler;

@Component
public class GaStartGeneralApplicationBusinessProcessExternalTaskListener {

    private static final String TOPIC = "START_GA_BUSINESS_PROCESS";

    @Autowired
    private GaStartGeneralApplicationBusinessProcessExternalTaskListener(
            GaStartGeneralApplicationBusinessProcessTaskHandler startGeneralApplicationBusinessProcessTaskHandler,
            ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(startGeneralApplicationBusinessProcessTaskHandler).open();
    }
}
