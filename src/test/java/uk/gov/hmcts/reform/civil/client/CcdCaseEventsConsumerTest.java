package uk.gov.hmcts.reform.civil.client;

import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusErrorSource;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import uk.gov.hmcts.reform.civil.config.CcdEventServiceBusConfiguration;
import uk.gov.hmcts.reform.civil.service.servicebus.CcdEventMessageReceiverService;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@ExtendWith(MockitoExtension.class)
public class CcdCaseEventsConsumerTest {

    @Mock
    private CcdEventServiceBusConfiguration ccdEventServiceBusConfiguration;

    @Mock
    private CcdEventMessageReceiverService ccdEventMessageReceiverService;

    @Mock
    private ServiceBusSessionReceiverClient sessionReceiverClient;
    @Mock
    private ServiceBusReceiverClient receiverClient;

    @Mock
    private ServiceBusReceivedMessage receivedMessage;

    @InjectMocks
    private CcdCaseEventsConsumer ccdCaseEventsConsumer;

    @Test
    void given_session_is_accepted_when_receiver_throws_error() {
        when(sessionReceiverClient.acceptNextSession()).thenThrow(IllegalStateException.class);

        ccdCaseEventsConsumer.consumeMessage(sessionReceiverClient);

        verify(receiverClient, Mockito.times(0)).complete(receivedMessage);
        verify(receiverClient, Mockito.times(0)).abandon(any());
        verify(receiverClient, Mockito.times(0)).deadLetter(any(), any());
    }

    @Test
    void given_session_is_accepted_when_receiver_complete_throws_error() {
        when(receivedMessage.getBody()).thenReturn(BinaryData.fromString("TestMessage"));

        publishMessageToReceiver();

        doThrow(new ServiceBusException(new Exception(), ServiceBusErrorSource.UNKNOWN)).doNothing()
            .when(receiverClient).complete(receivedMessage);

        ccdCaseEventsConsumer.consumeMessage(sessionReceiverClient);

        verify(receiverClient, Mockito.times(1)).complete(receivedMessage);
        verify(receiverClient, Mockito.times(1)).abandon(receivedMessage);
        verify(receiverClient, Mockito.times(0)).deadLetter(any(), any());
    }

    @Test
    void given_session_is_accepted_when_receiver_complete_throws_error_on_both_calls() {
        when(receivedMessage.getBody()).thenReturn(BinaryData.fromString("TestMessage"));

        publishMessageToReceiver();

        doThrow(new ServiceBusException(new Exception(), ServiceBusErrorSource.UNKNOWN))
            .when(receiverClient).complete(receivedMessage);
        doThrow(new ServiceBusException(new Exception(), ServiceBusErrorSource.UNKNOWN))
            .when(receiverClient).abandon(receivedMessage);


        ccdCaseEventsConsumer.consumeMessage(sessionReceiverClient);

        verify(receiverClient, Mockito.times(1)).complete(receivedMessage);
        verify(receiverClient, Mockito.times(1)).abandon(receivedMessage);
        verify(receiverClient, Mockito.times(0)).deadLetter(any(), any());
    }

    @Test
    void given_session_is_accepted_when_handling_message_throws_error() throws JsonProcessingException {
        when(receivedMessage.getBody()).thenReturn(BinaryData.fromString("TestMessage"));

        publishMessageToReceiver();

        doThrow(new RuntimeException()).when(ccdEventMessageReceiverService)
            .handleCcdCaseEventAsbMessage(any(), any(), any());

        ccdCaseEventsConsumer.consumeMessage(sessionReceiverClient);

        verify(receiverClient, Mockito.times(1)).abandon(receivedMessage);
        verify(receiverClient, Mockito.times(0)).complete(any());
        verify(receiverClient, Mockito.times(0)).deadLetter(any(), any());
    }

    @Test
    void given_session_is_accepted_when_message_is_consumed() {
        when(receivedMessage.getBody()).thenReturn(BinaryData.fromString("TestMessage"));

        publishMessageToReceiver();

        doNothing().when(receiverClient).complete(receivedMessage);

        ccdCaseEventsConsumer.consumeMessage(sessionReceiverClient);

        verify(receiverClient, Mockito.times(1)).complete(receivedMessage);
    }

    @Test
    void should_start_consume_messages_when_consumer_start_is_called() {
        when(ccdEventServiceBusConfiguration.createCcdCaseEventsSessionReceiver()).thenReturn(sessionReceiverClient);
        when(sessionReceiverClient.acceptNextSession()).thenReturn(receiverClient);
        when(receiverClient.receiveMessages(1)).thenReturn(new IterableStream<>(Flux.empty()));

        Thread consumer = new Thread(ccdCaseEventsConsumer);
        consumer.start();

        await()
            .atMost(1, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(sessionReceiverClient, atLeastOnce()).acceptNextSession();
                verify(receiverClient, atLeastOnce()).receiveMessages(1);
            });
        ccdCaseEventsConsumer.stop();
    }

    @Test
    void should_stop_consume_messages_when_consumer_stop_is_called() {
        ccdCaseEventsConsumer.stop();
        Thread consumer = new Thread(ccdCaseEventsConsumer);
        consumer.start();

        verify(sessionReceiverClient, never()).acceptNextSession();
        verify(receiverClient, never()).receiveMessages(1);
    }

    private void publishMessageToReceiver() {
        when(sessionReceiverClient.acceptNextSession()).thenReturn(receiverClient);

        final Flux<ServiceBusReceivedMessage> iterableStreamFlux = Flux.<ServiceBusReceivedMessage>create(
            sink -> {
                sink.next(receivedMessage);
                sink.complete();
            }).subscribeOn(Schedulers.single());

        when(receiverClient.receiveMessages(1)).thenReturn(new IterableStream<>(iterableStreamFlux));
    }

    @Test
    void should_not_throw_npe_if_receiver_is_null() {

        when(sessionReceiverClient.acceptNextSession()).thenReturn(null);

        ccdCaseEventsConsumer.consumeMessage(sessionReceiverClient);

        verify(receiverClient, Mockito.times(0)).complete(receivedMessage);
        verify(receiverClient, Mockito.times(0)).abandon(any());
        verify(receiverClient, Mockito.times(0)).deadLetter(any(), any());

    }

}
