package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.CheckUnlessOrderDeadlineEndTaskHandler;

@Component
public class GAUnlessOrderSchedulerExternalTaskListener {

    private static final String TOPIC = "GAUnlessOrderScheduler";

    @Autowired
    private GAUnlessOrderSchedulerExternalTaskListener(CheckUnlessOrderDeadlineEndTaskHandler taskHandler,
                                                       ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(taskHandler).open();
    }
}
