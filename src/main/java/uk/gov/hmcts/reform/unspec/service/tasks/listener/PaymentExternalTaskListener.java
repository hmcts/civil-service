package uk.gov.hmcts.reform.unspec.service.tasks.listener;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.unspec.service.tasks.handler.PaymentTaskHandler;

@Component
public class PaymentExternalTaskListener {

    private static final String TOPIC = "processPayment";

    @Autowired
    private PaymentExternalTaskListener(PaymentTaskHandler paymentTaskHandler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(paymentTaskHandler).open();
    }
}
