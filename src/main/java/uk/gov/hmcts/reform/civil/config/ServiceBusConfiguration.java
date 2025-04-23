package uk.gov.hmcts.reform.civil.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ServiceBusConfiguration {

    @Value("${azure.service-bus.hmc-to-hearings-api.namespace}")
    private String namespace;

    @Value("${azure.service-bus.connection-postfix}")
    private String connectionPostfix;

    @Value("${azure.service-bus.hmc-to-hearings-api.username}")
    private String username;

    @Value("${azure.service-bus.hmc-to-hearings-api.password}")
    private String password;

    @Value("${azure.service-bus.hmc-to-hearings-api.topicName}")
    private String topicName;

    @Value("${azure.service-bus.hmc-to-hearings-api.subscriptionName}")
    private String subscriptionName;

    private final ObjectMapper objectMapper;
    private final HmcMessageHandler handler;

    private ServiceBusProcessorClient processorClient;

    @Bean
    @ConditionalOnProperty(value = "azure.service-bus.hmc-to-hearings-api.enabled", havingValue = "true")
    public ServiceBusProcessorClient serviceBusProcessorClient() throws URISyntaxException {
        URI endpoint = new URI("sb://" + namespace + connectionPostfix);

        String connectionString = "Endpoint=" + endpoint + "/"
            + ";SharedAccessKeyName=" + username
            + ";SharedAccessKey=" + password;

        processorClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .processor()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .processMessage(this::processMessage)
            .processError(this::processError)
            .buildProcessorClient();

        processorClient.start();
        log.info("HMC ServiceBusProcessorClient started successfully.");

        return processorClient;
    }

    private void processMessage(ServiceBusReceivedMessageContext context) {
        try {
            log.info("HMC Message Received");
            ServiceBusReceivedMessage message = context.getMessage();

            byte[] bodyBytes = message.getBody().toBytes();
            HmcMessage hmcMessage = objectMapper.readValue(bodyBytes, HmcMessage.class);

            log.info(
                "HMC Message for case {}, hearing id {} with status {}",
                hmcMessage.getCaseId(),
                hmcMessage.getHearingId(),
                Optional.ofNullable(hmcMessage.getHearingUpdate())
                    .map(update -> update.getHmcStatus().name())
                    .orElse("-")
            );

            handler.handleMessage(hmcMessage);
            context.complete();
        } catch (Exception e) {
            log.error("There was a problem processing the message: {}", e.getMessage(), e);
            context.abandon();
        }
    }

    private void processError(ServiceBusErrorContext context) {
        log.error("Exception occurred while processing message");
        log.error("{} - {}", context.getErrorSource(), context.getException().getMessage(), context.getException());
    }
}
