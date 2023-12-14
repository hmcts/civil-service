package uk.gov.hmcts.reform.civil.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.MessageHandlerOptions;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.civil.handler.ReviewHearingExceptionHandler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.hmc.model.messaging.HmcMessage;

import static java.util.Optional.ofNullable;

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

    @Value("${azure.service-bus.thread-count}")
    private int threadCount;

    private final ObjectMapper objectMapper;
    private final ReviewHearingExceptionHandler handler;
    private final FeatureToggleService featureToggleService;

    @Bean
    @ConditionalOnProperty("azure.service-bus.hmc-to-hearings-api.enabled")
    public SubscriptionClient receiveClient()
        throws URISyntaxException, ServiceBusException, InterruptedException {
        if (featureToggleService.isAutomatedHearingNoticeEnabled()) {
            URI endpoint = new URI("sb://" + namespace + connectionPostfix);

            String destination = topicName.concat("/subscriptions/").concat(subscriptionName);

            ConnectionStringBuilder connectionStringBuilder =
                new ConnectionStringBuilder(
                    endpoint, destination, username, password);
            connectionStringBuilder.setOperationTimeout(Duration.ofMinutes(10));
            return new SubscriptionClient(connectionStringBuilder, ReceiveMode.PEEKLOCK);
        }
        return null;
    }

    @Bean
    @ConditionalOnProperty("azure.service-bus.hmc-to-hearings-api.enabled")
    CompletableFuture<Void> registerMessageHandlerOnClient(
        @Autowired SubscriptionClient receiveClient)
        throws ServiceBusException, InterruptedException {
        if (featureToggleService.isAutomatedHearingNoticeEnabled()) {
            IMessageHandler messageHandler =
                new IMessageHandler() {

                    @SneakyThrows
                    @Override
                    public CompletableFuture<Void> onMessageAsync(IMessage message) {
                        boolean exceptionEventTriggered = false;
                        log.info("message received");
                        List<byte[]> body = message.getMessageBody().getBinaryData();

                        HmcMessage hmcMessage = objectMapper.readValue(body.get(0), HmcMessage.class);
                        log.info(
                                "HMC Message for case {}, hearing id {} with status {}",
                                hmcMessage.getCaseId(),
                                hmcMessage.getHearingId(),
                                ofNullable(hmcMessage.getHearingUpdate()).map(update -> update.getHmcStatus().name())
                                        .orElse("-")
                        );
                        exceptionEventTriggered = handler.handleExceptionEvent(hmcMessage);
                        if (exceptionEventTriggered) {
                            return receiveClient.completeAsync(message.getLockToken());
                        }
                        return receiveClient.abandonAsync(message.getLockToken());
                    }

                    @Override
                    public void notifyException(
                        Throwable throwable, ExceptionPhase exceptionPhase) {
                        log.error("Exception occurred.");
                        log.error(exceptionPhase + "-" + throwable.getMessage());
                    }
                };

            ExecutorService executorService = Executors.newFixedThreadPool(4);
            receiveClient.registerMessageHandler(
                messageHandler,
                new MessageHandlerOptions(
                    4, false,
                    Duration.ofHours(1), Duration.ofMinutes(5)
                ),
                executorService
            );
            return null;
        }
        return null;
    }
}
