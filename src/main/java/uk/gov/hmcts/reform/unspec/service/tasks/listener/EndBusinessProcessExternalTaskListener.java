package uk.gov.hmcts.reform.unspec.service.tasks.listener;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.unspec.service.tasks.handler.EndBusinessProcessTaskHandler;

@Component
public class EndBusinessProcessExternalTaskListener {

    private static final String TOPIC = "END_BUSINESS_PROCESS";

    @Autowired
    private EndBusinessProcessExternalTaskListener(EndBusinessProcessTaskHandler handler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(handler).open();
    }
}
