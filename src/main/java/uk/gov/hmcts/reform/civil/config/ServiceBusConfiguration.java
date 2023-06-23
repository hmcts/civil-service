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
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.hmc.model.messaging.HmcMessage;
import uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus;

@Configuration
@Slf4j
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

    // @Value("${thread.count}")
    // private int threadCount;

    @Bean
    public SubscriptionClient receiveClient()
        throws URISyntaxException, ServiceBusException, InterruptedException {
        if (!namespace.contains("aat")) {
            log.info("namespace: {}", namespace);
            log.info("connectionPostfix: {}", connectionPostfix);
            log.info("topicName: {}", topicName);
            log.info("subscriptionName: {}", subscriptionName);
            log.info("username: {}", username);
            URI endpoint = new URI("sb://" + namespace + connectionPostfix);
            log.info("endpoint: {}", endpoint);

            String destination = topicName.concat("/subscriptions/").concat(subscriptionName);
            log.info("destination: {}", destination);

            ConnectionStringBuilder connectionStringBuilder =
                new ConnectionStringBuilder(
                    endpoint, destination, username, password);
            connectionStringBuilder.setOperationTimeout(Duration.ofMinutes(10));
            return new SubscriptionClient(connectionStringBuilder, ReceiveMode.PEEKLOCK);
        }
        return null;
    }

    @Bean
    CompletableFuture<Void> registerMessageHandlerOnClient(
        @Autowired SubscriptionClient receiveClient)
        throws ServiceBusException, InterruptedException {
        if (!namespace.contains("aat")) {
            IMessageHandler messageHandler =
                new IMessageHandler() {

                    @SneakyThrows
                    @Override
                    public CompletableFuture<Void> onMessageAsync(IMessage message) {
                        String now = LocalDateTime.now().toString();
                        log.info("message received {}", now);
                        List<byte[]> body = message.getMessageBody().getBinaryData();
                        ObjectMapper mapper = new ObjectMapper();

                        HmcMessage hearing = mapper.readValue(body.get(0), HmcMessage.class);
                        String listAssistSessionID = hearing.getHearingUpdate().getListAssistSessionID();
                        log.info("message received: {}", listAssistSessionID);
                        if (HmcStatus.EXCEPTION.equals(hearing.getHearingUpdate().getHmcStatus())) {
                            log.info("triggering WA event");
                            // trigger ccd event for WA
                            // if event triggered successfully
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
                    4, false, Duration.ofHours(1), Duration.ofMinutes(5)),
                executorService
            );
            return null;
        }
        return null;
    }
}
