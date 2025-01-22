package uk.gov.hmcts.reform.civil.config;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;


@Slf4j
@Component
@Scope("prototype")
@ConditionalOnExpression("${azure.servicebus.ccd-events-topic.enabled:true}")
public class CcdEventServiceBusConfiguration {

    @Value("${azure.servicebus.ccd-events-topic.connection-string}")
    private String connectionString;
    @Value("${azure.servicebus.ccd-events-topic.topic-name}")
    private String topicName;
    @Value("${azure.servicebus.ccd-events-topic.civil-ccd-case-events-subscription-name}")
    private String ccdCaseEventsSubscriptionName;
    @Value("${azure.servicebus.ccd-events-topic.retry-duration}")
    private int retryTime;

    public ServiceBusSessionReceiverClient createCcdCaseEventsSessionReceiver() {
        log.info("Creating CCD Case Events Session receiver");
        ServiceBusSessionReceiverClient client = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .retryOptions(retryOptions())
            .sessionReceiver()
            .topicName(topicName)
            .subscriptionName(ccdCaseEventsSubscriptionName)
            .buildClient();

        log.info("CCD Case Events Session receiver created successfully");
        return client;
    }

    private AmqpRetryOptions retryOptions() {
        AmqpRetryOptions retryOptions = new AmqpRetryOptions();
        retryOptions.setTryTimeout(Duration.ofSeconds(retryTime));
        return retryOptions;
    }
}
