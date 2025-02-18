package uk.gov.hmcts.reform.civil.client;

import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.CcdEventServiceBusConfiguration;
import uk.gov.hmcts.reform.civil.service.servicebus.CcdEventMessageReceiverService;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SuppressWarnings("PMD.DoNotUseThreads")
public class CcdCaseEventsConsumer implements Runnable {

    private final CcdEventServiceBusConfiguration ccdEventServiceBusConfiguration;
    private final CcdEventMessageReceiverService eventMessageReceiverService;
    private boolean keepRun = true;

    public CcdCaseEventsConsumer(CcdEventServiceBusConfiguration ccdEventServiceBusConfiguration,
                                 CcdEventMessageReceiverService eventMessageReceiverService) {
        this.ccdEventServiceBusConfiguration = ccdEventServiceBusConfiguration;
        this.eventMessageReceiverService = eventMessageReceiverService;
    }

    @Override
    public void run() {
        try (ServiceBusSessionReceiverClient sessionReceiver =
                 ccdEventServiceBusConfiguration.createCcdCaseEventsSessionReceiver()) {
            while (keepRun) {
                consumeMessage(sessionReceiver);
            }
        }
    }

    protected void consumeMessage(ServiceBusSessionReceiverClient sessionReceiver) {
        try (ServiceBusReceiverClient receiver = sessionReceiver.acceptNextSession()) {

            if (receiver == null) {
                log.warn("ServiceBusReceiverClient receiver was null.");
                return;
            }

            receiver.receiveMessages(1).forEach(
                message -> {
                    try {
                        String messageId = message.getMessageId();
                        String sessionId = message.getSessionId();
                        log.info("Received CCD Case Event message with id '{}' and case id '{}'",
                                 messageId, sessionId);

                        eventMessageReceiverService.handleCcdCaseEventAsbMessage(messageId,
                                                                                 sessionId,
                                                                                 new String(message.getBody().toBytes()));
                        receiver.complete(message);

                        log.info("CCD Case Event message with id '{}' handled successfully", messageId);
                    } catch (Exception ex) {
                        log.error("Error processing CCD Case Event message with id '{}' - "
                                      + "abandon the processing and ASB will re-deliver it", message.getMessageId());
                        receiver.abandon(message);
                    }
                });
        } catch (IllegalStateException ex) {
            log.info("Timeout: No CCD Case Event messages received waiting for next session {}", ex.getMessage());
        } catch (ServiceBusException ex) {
            log.error("Error occurred while receiving messages {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error occurred while closing the session {}", ex.getMessage());
        }
    }

    public void stop() {
        keepRun = false;
    }

}
