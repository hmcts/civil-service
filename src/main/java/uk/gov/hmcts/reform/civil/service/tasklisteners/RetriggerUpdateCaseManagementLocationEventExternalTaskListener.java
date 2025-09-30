package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.RetriggerUpdateCaseMgmtLocationDataHandler;

@Component
public class RetriggerUpdateCaseManagementLocationEventExternalTaskListener {

    private static final String TOPIC = "RETRIGGER_GA_UPDATE_CMLOCATION_EVENTS";

    @Autowired
    private RetriggerUpdateCaseManagementLocationEventExternalTaskListener(
        RetriggerUpdateCaseMgmtLocationDataHandler retriggerUpdateCaseMgmtLocationDataHandler,
                                                            ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(retriggerUpdateCaseMgmtLocationDataHandler).open();
    }
}
