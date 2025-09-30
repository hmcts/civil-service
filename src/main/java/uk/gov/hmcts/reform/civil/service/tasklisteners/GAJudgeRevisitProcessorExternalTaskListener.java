package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.GAJudgeRevisitTaskHandler;

@Component
@ConditionalOnExpression("${response.deadline.check.event.emitter.enabled:true}")
public class GAJudgeRevisitProcessorExternalTaskListener {

    private static final String TOPIC = "GAJudgeRevisitProcessor";

    @Autowired
    private GAJudgeRevisitProcessorExternalTaskListener(GAJudgeRevisitTaskHandler taskHandler,
                                                        ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(taskHandler).open();
    }
}
