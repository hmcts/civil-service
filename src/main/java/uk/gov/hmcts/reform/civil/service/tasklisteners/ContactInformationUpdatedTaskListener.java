package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.ContactInformationUpdatedTaskHandler;

@Component
public class ContactInformationUpdatedTaskListener {

    private static final String TOPIC = "processEventWithDescription";

    @Autowired
    private ContactInformationUpdatedTaskListener(ContactInformationUpdatedTaskHandler handler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(handler).open();
    }
}
