package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.EndGaDocUploadProcessTaskHandler;

@Component
public class EndDocUploadBusinessProcessExternalTaskListener {

    private static final String TOPIC = "END_DOC_UPLOAD_BUSINESS_PROCESS_GASPEC";

    @Autowired
    private EndDocUploadBusinessProcessExternalTaskListener(
        EndGaDocUploadProcessTaskHandler handler,
        ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(handler).open();
    }
}
