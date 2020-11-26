package uk.gov.hmcts.reform.unspec.service.tasks.listener;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.unspec.service.tasks.handler.ClaimStrikeoutHandler;

@Component
public class CaseStrikeOutExternalTaskListener {

    private static final String TOPIC = "CASE_STRIKEOUT";

    @Autowired
    private CaseStrikeOutExternalTaskListener(ClaimStrikeoutHandler caseStrikeoutHandler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(caseStrikeoutHandler).open();
    }
}
