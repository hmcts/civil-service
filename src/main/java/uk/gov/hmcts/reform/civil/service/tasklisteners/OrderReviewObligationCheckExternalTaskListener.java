package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.tasks.OrderReviewObligationCheckHandler;

@Component
public class OrderReviewObligationCheckExternalTaskListener {

    private static final String TOPIC = "ORDER_REVIEW_OBLIGATION_CHECK";

    @Autowired
    private OrderReviewObligationCheckExternalTaskListener(OrderReviewObligationCheckHandler handler, ExternalTaskClient client) {
        TopicSubscriptionBuilder subscriptionBuilder = client.subscribe(TOPIC);
        subscriptionBuilder.handler(handler).open();
    }
}
