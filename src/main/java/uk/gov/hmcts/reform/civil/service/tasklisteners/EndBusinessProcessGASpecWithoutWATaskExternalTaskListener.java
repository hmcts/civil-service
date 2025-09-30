package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.EndBusinessProcessGASpecWithoutWATaskHandler;

@Component
public class EndBusinessProcessGASpecWithoutWATaskExternalTaskListener {

    private static final String TOPIC = "END_BUSINESS_PROCESS_GASPEC_WITHOUT_WA_TASK";

    @Autowired
    private EndBusinessProcessGASpecWithoutWATaskExternalTaskListener(
        EndBusinessProcessGASpecWithoutWATaskHandler handler,
        ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(handler).open();
    }
}
