package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.WaitCivilDocUpdatedTaskHandler;

@Component
public class WaitCivilDocUpdatedExternalTaskListener {

    private static final String TOPIC = "WAIT_CIVIL_DOC_UPDATED_GASPEC";

    @Autowired
    private WaitCivilDocUpdatedExternalTaskListener(
            WaitCivilDocUpdatedTaskHandler handler,
            ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(handler).open();
    }
}
