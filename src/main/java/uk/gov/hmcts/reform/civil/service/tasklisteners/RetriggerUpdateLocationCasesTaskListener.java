package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.RetriggerUpdateLocationDataHandler;

@Component
public class RetriggerUpdateLocationCasesTaskListener {

    private static final String TOPIC = "RETRIGGER_UPDATE_LOCATION_EVENTS";

    @Autowired
    private RetriggerUpdateLocationCasesTaskListener(RetriggerUpdateLocationDataHandler updateLocationDataHandler,
                                                     ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(updateLocationDataHandler).open();
    }
}
