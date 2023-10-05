package uk.gov.hmcts.reform.migration.migration;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.ClaimDismissedHandler;

@Component
public class MigrationExternalTaskListener {

    private static final String TOPIC = "START_MIGRATION";

    @Autowired
    private MigrationExternalTaskListener(MigrationExternalTaskHandler migrationHandler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(migrationHandler).open();
    }
}
