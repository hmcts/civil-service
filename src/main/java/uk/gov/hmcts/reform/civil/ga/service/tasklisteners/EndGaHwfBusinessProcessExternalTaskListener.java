package uk.gov.hmcts.reform.civil.ga.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.handler.tasks.EndGaHwfNotifyProcessTaskHandler;

@Component
public class EndGaHwfBusinessProcessExternalTaskListener {

    private static final String TOPIC = "END_GA_HWF_NOTIFY_PROCESS";

    @Autowired
    private EndGaHwfBusinessProcessExternalTaskListener(
        EndGaHwfNotifyProcessTaskHandler handler,
        ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(handler).open();
    }
}
