package uk.gov.hmcts.reform.civil.config;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import com.azure.messaging.servicebus.models.SubQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;


@Slf4j
@Component
@Scope("prototype")
@ConditionalOnExpression("${azure.servicebus.enableASB-DLQ:true}")
public class CcdEventServiceBusConfiguration {

    @Value("${azure.servicebus.connection-string}")
    private String connectionString;
    @Value("${azure.servicebus.topic-name}")
    private String topicName;
    @Value("${azure.servicebus.ccd-case-events-subscription-name}")
    private String ccdCaseEventsSubscriptionName;
    @Value("${azure.servicebus.retry-duration}")
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

        log.info("CCD Case Events Session receiver created, successfully");
        return client;
    }

    public ServiceBusReceiverClient createCcdCaseEventsDeadLetterQueueSessionReceiver() {
        log.info("Creating CCD Case Events Dead Letter Queue Session receiver");
        ServiceBusReceiverClient client = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .retryOptions(retryOptions())
            .receiver()
            .topicName(topicName)
            .subQueue(SubQueue.DEAD_LETTER_QUEUE)
            .subscriptionName(ccdCaseEventsSubscriptionName)
            .buildClient();

        log.info("CCD Case Events Dead Letter Queue Session receiver created, successfully");
        return client;
    }

    private AmqpRetryOptions retryOptions() {
        AmqpRetryOptions retryOptions = new AmqpRetryOptions();
        retryOptions.setTryTimeout(Duration.ofSeconds(Integer.valueOf(retryTime)));
        return retryOptions;
    }
}
