package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.EvidenceUploadCheckHandler;

@Component
public class EvidenceUploadNotificationExternalTaskListener {

    private static final String TOPIC = "EVIDENCE_UPLOAD_CHECK";

    @Autowired
    private EvidenceUploadNotificationExternalTaskListener(EvidenceUploadCheckHandler evidenceUploadCheckHandler,
                                                           ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(evidenceUploadCheckHandler).open();
    }
}
