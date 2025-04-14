package uk.gov.hmcts.reform.civil.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.civil.handler.HmcMessageHandler;
import uk.gov.hmcts.reform.hmc.model.messaging.HmcMessage;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ServiceBusConfiguration {

    @Value("${azure.service-bus.hmc-to-hearings-api.connection-string}")
    private String connectionString;

    @Value("${azure.service-bus.hmc-to-hearings-api.topicName}")
    private String topicName;

    @Value("${azure.service-bus.hmc-to-hearings-api.subscriptionName}")
    private String subscriptionName;

    private final ObjectMapper objectMapper;
    private final HmcMessageHandler handler;

    private ServiceBusProcessorClient processorClient;

    @Bean
    @ConditionalOnProperty(value = "azure.service-bus.hmc-to-hearings-api.enabled", havingValue = "true")
    public ServiceBusProcessorClient serviceBusProcessorClient() {
        log.info("ConditionalOnProperty  is TRUE");
        processorClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .processor()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .processMessage(this::processMessage)
            .processError(context -> log.error("Error receiving message", context.getException()))
            .buildProcessorClient();
        return processorClient;
    }

    @PostConstruct
    public void startProcessor() {
        if (processorClient != null) {
            processorClient.start();
            log.info("HMC ServiceBusProcessorClient started successfully.");
        } else {
            log.error("HMC ServiceBusProcessorClient is not initialized properly.");
        }
    }

    private void processMessage(ServiceBusReceivedMessageContext context) {
        try {
            log.info("NEW TEST HMC Message Received");
            ServiceBusReceivedMessage message = context.getMessage();
            byte[] body = message.getBody().toBytes();

            HmcMessage hmcMessage = objectMapper.readValue(body, HmcMessage.class);
            log.info(
                "NEW TEST HMC Message for case {}, hearing id {} with status {}",
                hmcMessage.getCaseId(),
                hmcMessage.getHearingId(),
                Optional.ofNullable(hmcMessage.getHearingUpdate())
                    .map(update -> update.getHmcStatus().name())
                    .orElse("-")
            );

            handler.handleMessage(hmcMessage);
            context.complete();
        } catch (Exception e) {
            log.error("NEW TEST There was a problem processing the message: {}", e.getMessage(), e);
            context.abandon();
        }
    }
}
