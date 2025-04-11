package uk.gov.hmcts.reform.civil.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.civil.handler.HmcMessageHandler;
import uk.gov.hmcts.reform.hmc.model.messaging.HmcMessage;

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

    @Bean
    @ConditionalOnProperty("azure.service-bus.hmc-to-hearings-api.enabled")
    public ServiceBusProcessorClient serviceBusProcessorClient() {
        return new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .processor()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .processMessage(this::processMessage)
            .processError(context -> log.error("Error receiving message", context.getException()))
            .buildProcessorClient();
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
