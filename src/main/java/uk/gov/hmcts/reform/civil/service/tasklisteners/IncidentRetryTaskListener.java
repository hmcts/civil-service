package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.IncidentRetryEventHandler;

@Component
public class IncidentRetryTaskListener {

    private static final String TOPIC = "INCIDENT_RETRY_EVENT";

    @Autowired
    public IncidentRetryTaskListener(IncidentRetryEventHandler incidentRetryEventHandler,
                                     ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(incidentRetryEventHandler).open();
    }
}
