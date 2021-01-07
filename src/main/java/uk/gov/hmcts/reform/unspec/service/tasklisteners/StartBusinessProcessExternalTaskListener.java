package uk.gov.hmcts.reform.unspec.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.unspec.handler.tasks.StartBusinessProcessTaskHandler;

@Component
public class StartBusinessProcessExternalTaskListener {

    private static final String TOPIC = "START_BUSINESS_PROCESS";

    @Autowired
    private StartBusinessProcessExternalTaskListener(StartBusinessProcessTaskHandler startBusinessProcessTaskHandler,
                                                     ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(startBusinessProcessTaskHandler).open();
    }
}
