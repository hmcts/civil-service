package uk.gov.hmcts.reform.civil.ga.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.handler.tasks.EndGeneralApplicationBusinessProcessTaskHandler;

@Component
public class EndBusinessProcessExternalTaskListener {

    private static final String TOPIC = "END_BUSINESS_PROCESS_GASPEC";

    @Autowired
    private EndBusinessProcessExternalTaskListener(
            EndGeneralApplicationBusinessProcessTaskHandler handler,
            ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(handler).open();
    }
}
