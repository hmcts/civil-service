package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.EndJudgeMakesDecisionBusinessProcessTaskHandler;

@Component
public class EndJudgeBusinessProcessExternalTaskListener {

    private static final String TOPIC = "END_JUDGE_BUSINESS_PROCESS_GASPEC";

    @Autowired
    private EndJudgeBusinessProcessExternalTaskListener(
        EndJudgeMakesDecisionBusinessProcessTaskHandler handler,
        ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(handler).open();
    }
}
