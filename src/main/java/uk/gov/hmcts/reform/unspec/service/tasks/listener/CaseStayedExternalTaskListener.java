package uk.gov.hmcts.reform.unspec.service.tasks.listener;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.unspec.service.tasks.handler.CaseStayedHandler;

@Component
public class CaseStayedExternalTaskListener {

    private static final String TOPIC = "CASE_STAYED_FINDER";

    @Autowired
    private CaseStayedExternalTaskListener(CaseStayedHandler caseStayedFinder, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(caseStayedFinder).open();
    }
}
