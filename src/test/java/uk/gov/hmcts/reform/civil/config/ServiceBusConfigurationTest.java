package uk.gov.hmcts.reform.civil.config;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusProcessorClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.civil.handler.HmcMessageHandler;
import uk.gov.hmcts.reform.hmc.model.messaging.HmcMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceBusConfigurationTest {

    @Mock
    private HmcMessageHandler handler;
    @Mock
    private ServiceBusReceivedMessageContext context;
    @Mock
    private ServiceBusReceivedMessage message;

    private ServiceBusConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new ServiceBusConfiguration(new ObjectMapper(), handler);
    }

    @Test
    void shouldDisableAutomaticSettlement() throws Exception {
        ServiceBusProcessorClientBuilder processorBuilder = mock(ServiceBusProcessorClientBuilder.class, RETURNS_SELF);
        ServiceBusProcessorClient processor = mock(ServiceBusProcessorClient.class);
        when(processorBuilder.buildProcessorClient()).thenReturn(processor);
        ReflectionTestUtils.setField(configuration, "namespace", "test");
        ReflectionTestUtils.setField(configuration, "connectionPostfix", ".servicebus.windows.net");
        ReflectionTestUtils.setField(configuration, "username", "test-user");
        ReflectionTestUtils.setField(configuration, "password", "test-key");
        ReflectionTestUtils.setField(configuration, "topicName", "test-topic");
        ReflectionTestUtils.setField(configuration, "subscriptionName", "test-subscription");

        try (MockedConstruction<ServiceBusClientBuilder> ignored = mockConstruction(
            ServiceBusClientBuilder.class, (builder, constructionContext) -> {
                when(builder.connectionString(anyString())).thenReturn(builder);
                when(builder.processor()).thenReturn(processorBuilder);
            })) {
            assertThat(configuration.serviceBusProcessorClient()).isSameAs(processor);
            verify(processorBuilder).disableAutoComplete();
            verify(processor).start();
        }
    }

    @Test
    void shouldCompleteSuccessfullyProcessedMessage() {
        stubBody("{}");

        ReflectionTestUtils.invokeMethod(configuration, "processMessage", context);

        verify(handler).handleMessage(any(HmcMessage.class));
        verify(context).complete();
        verify(context, never()).abandon();
    }

    @Test
    void shouldAbandonMessageWhenHandlerFails() {
        stubBody("{}");
        doThrow(new IllegalStateException("Event failed")).when(handler).handleMessage(any(HmcMessage.class));

        ReflectionTestUtils.invokeMethod(configuration, "processMessage", context);

        verify(context).abandon();
        verify(context, never()).complete();
    }

    @Test
    void shouldAbandonMalformedMessage() {
        stubBody("invalid-json");

        ReflectionTestUtils.invokeMethod(configuration, "processMessage", context);

        verifyNoInteractions(handler);
        verify(context).abandon();
        verify(context, never()).complete();
    }

    private void stubBody(String body) {
        when(context.getMessage()).thenReturn(message);
        when(message.getBody()).thenReturn(BinaryData.fromString(body));
    }
}
