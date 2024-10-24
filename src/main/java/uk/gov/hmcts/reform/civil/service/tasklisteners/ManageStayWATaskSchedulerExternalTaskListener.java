package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.ManageStayWATaskSchedulerHandler;

@Component
public class ManageStayWATaskSchedulerExternalTaskListener {

    private static final String TOPIC = "MANAGE_STAY_WA_TASK_SCHEDULER";

    @Autowired
    private ManageStayWATaskSchedulerExternalTaskListener(ManageStayWATaskSchedulerHandler manageStayWATaskSchedulerHandler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(manageStayWATaskSchedulerHandler).open();
    }
}
