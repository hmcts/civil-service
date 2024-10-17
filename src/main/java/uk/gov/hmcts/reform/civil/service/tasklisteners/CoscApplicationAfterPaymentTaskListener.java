package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.CoscApplicationAfterPaymentTaskHandler;

@Component
public class CoscApplicationAfterPaymentTaskListener {

    private static final String TOPIC = "coscApplicationAfterPayment";

    @Autowired
    private CoscApplicationAfterPaymentTaskListener(CoscApplicationAfterPaymentTaskHandler coscApplicationAfterPaymentTaskHandler,
                                                    ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(coscApplicationAfterPaymentTaskHandler).open();
    }
}
