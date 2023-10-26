package uk.gov.hmcts.reform.civil.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.nexthearingdate.NextHearingDateVariables;
import uk.gov.hmcts.reform.hmc.model.messaging.HearingUpdate;
import uk.gov.hmcts.reform.hmc.model.messaging.HmcMessage;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REVIEW_HEARING_EXCEPTION;
import static uk.gov.hmcts.reform.civil.enums.nexthearingdate.UpdateType.DELETE;
import static uk.gov.hmcts.reform.civil.enums.nexthearingdate.UpdateType.UPDATE;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.ADJOURNED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.CANCELLED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.COMPLETED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.EXCEPTION;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.HEARING_REQUESTED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.LISTED;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    HmcMessageHandler.class
})
class HmcMessageHandlerTest {

    @Autowired
    private HmcMessageHandler handler;
    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @MockBean
    private PaymentsConfiguration paymentsConfiguration;
    @MockBean
    private RuntimeService runtimeService;
    @MockBean
    private ObjectMapper objectMapper;
    @Mock
    private ExternalTask mockTask;
    @Mock
    private MessageCorrelationBuilder messageCorrelationBuilder;

    static final String SERVICE_ID_KEY = "serviceId";
    static final String HEARING_ID = "hearing-id-1";
    static final String CASE_ID = "1111111111111111";
    static final String SERVICE_ID = "AAA7";
    static final String PROCESS_INSTANCE_ID = "process-instance-id";
    private static final String UPDATE_NEXT_HEARING_INFO_MESSAGE = "UpdateNextHearingInfo";

    @BeforeEach
    void setUp() {
        when(paymentsConfiguration.getSpecSiteId()).thenReturn("AAA6");
        when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");

        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getWorkerId()).thenReturn("worker");
        when(mockTask.getAllVariables()).thenReturn(new HashMap<>());
        when(mockTask.getVariable(SERVICE_ID_KEY)).thenReturn(SERVICE_ID);
        when(mockTask.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);
        when(runtimeService.createMessageCorrelation(any())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariables(any())).thenReturn(messageCorrelationBuilder);
    }

    @Nested
    class ExceptionStatusMessages {

        @Test
        void shouldTriggerEvent_whenMessageRHearingException() {
            HmcMessage hmcMessage = HmcMessage.builder()
                .caseId(1234L)
                .hearingId("HER1234")
                .hmctsServiceCode("AAA7")
                .hearingUpdate(HearingUpdate.builder()
                                   .hmcStatus(EXCEPTION)
                                   .build())
                .build();

            handler.handleExceptionEvent(hmcMessage);

            verify(coreCaseDataService).triggerEvent(1234L, REVIEW_HEARING_EXCEPTION);
        }

        @Test
        void shouldNotTriggerEvent_whenMessageNoHearingException() {
            HmcMessage hmcMessage = HmcMessage.builder()
                .caseId(1234L)
                .hearingId("HER1234")
                .hmctsServiceCode("AAA7")
                .hearingUpdate(HearingUpdate.builder()
                                   .hmcStatus(HEARING_REQUESTED)
                                   .build())
                .build();

            handler.handleExceptionEvent(hmcMessage);

            verifyNoInteractions(coreCaseDataService);
        }
    }

    @Nested
    class DispatchCamundaMessage {

        @Nested
        class UpdateMessage {

            @Test
            void shouldDispatchUpdateMessage_whenMessageListedStatus() {
                NextHearingDateVariables variables = NextHearingDateVariables.builder()
                    .caseId(Long.parseLong(CASE_ID))
                    .hearingId(HEARING_ID)
                    .nextHearingDate(LocalDateTime.now())
                    .hmcStatus(LISTED)
                    .build();

                when(objectMapper.convertValue(any(), eq(NextHearingDateVariables.class))).thenReturn(variables);
                handler.handleTask(mockTask);

                verify(runtimeService, times(1)).createMessageCorrelation(UPDATE_NEXT_HEARING_INFO_MESSAGE);
                verify(messageCorrelationBuilder, times(1)).setVariables(
                    NextHearingDateVariables.builder()
                        .caseId(Long.parseLong(CASE_ID))
                        .hearingId(HEARING_ID)
                        .nextHearingDate(LocalDateTime.now())
                        .updateType(UPDATE)
                        .build().toMap(objectMapper));
                verify(messageCorrelationBuilder, times(1)).correlateStartMessage();
                verifyNoInteractions(coreCaseDataService);
            }

            @Test
            void shouldNotDispatchUpdateMessage_whenMessageNotListedStatus() {
                NextHearingDateVariables variables = NextHearingDateVariables.builder()
                    .caseId(Long.parseLong(CASE_ID))
                    .hearingId(HEARING_ID)
                    .nextHearingDate(LocalDateTime.now())
                    .build();

                when(objectMapper.convertValue(any(), eq(NextHearingDateVariables.class))).thenReturn(variables);
                handler.handleTask(mockTask);

                verifyNoInteractions(runtimeService);
                verifyNoInteractions(messageCorrelationBuilder);
                verifyNoInteractions(coreCaseDataService);
            }
        }

        @Nested
        class DeleteMessage {

            @Test
            void shouldDispatchDeleteMessage_whenMessageCompletedStatus() {
                NextHearingDateVariables variables = NextHearingDateVariables.builder()
                    .caseId(Long.parseLong(CASE_ID))
                    .hearingId(HEARING_ID)
                    .nextHearingDate(LocalDateTime.now())
                    .hmcStatus(COMPLETED)
                    .build();

                when(objectMapper.convertValue(any(), eq(NextHearingDateVariables.class))).thenReturn(variables);
                handler.handleTask(mockTask);

                verify(runtimeService, times(1)).createMessageCorrelation(UPDATE_NEXT_HEARING_INFO_MESSAGE);
                verify(messageCorrelationBuilder, times(1)).setVariables(
                    NextHearingDateVariables.builder()
                        .caseId(Long.parseLong(CASE_ID))
                        .updateType(DELETE)
                        .build().toMap(objectMapper));
                verify(messageCorrelationBuilder, times(1)).correlateStartMessage();
                verifyNoInteractions(coreCaseDataService);
            }

            @Test
            void shouldDispatchDeleteMessage_whenMessageCancelledStatus() {
                NextHearingDateVariables variables = NextHearingDateVariables.builder()
                    .caseId(Long.parseLong(CASE_ID))
                    .hearingId(HEARING_ID)
                    .hmcStatus(CANCELLED)
                    .nextHearingDate(LocalDateTime.now())
                    .build();

                when(objectMapper.convertValue(any(), eq(NextHearingDateVariables.class))).thenReturn(variables);
                handler.handleTask(mockTask);

                verify(runtimeService, times(1)).createMessageCorrelation(UPDATE_NEXT_HEARING_INFO_MESSAGE);
                verify(messageCorrelationBuilder, times(1)).setVariables(
                    NextHearingDateVariables.builder()
                        .caseId(Long.parseLong(CASE_ID))
                        .updateType(DELETE)
                        .build().toMap(objectMapper));
                verify(messageCorrelationBuilder, times(1)).correlateStartMessage();
                verifyNoInteractions(coreCaseDataService);
            }
        }

        @Test
        void shouldDispatchDeleteMessage_whenMessageAdjournedStatus() {
            NextHearingDateVariables variables = NextHearingDateVariables.builder()
                .caseId(Long.parseLong(CASE_ID))
                .hearingId(HEARING_ID)
                .hmcStatus(ADJOURNED)
                .nextHearingDate(LocalDateTime.now())
                .build();

            when(objectMapper.convertValue(any(), eq(NextHearingDateVariables.class))).thenReturn(variables);
            handler.handleTask(mockTask);

            verify(runtimeService, times(1)).createMessageCorrelation(UPDATE_NEXT_HEARING_INFO_MESSAGE);
            verify(messageCorrelationBuilder, times(1)).setVariables(
                NextHearingDateVariables.builder()
                    .caseId(Long.parseLong(CASE_ID))
                    .updateType(DELETE)
                    .build().toMap(objectMapper));
            verify(messageCorrelationBuilder, times(1)).correlateStartMessage();
            verifyNoInteractions(coreCaseDataService);
        }

        @Test
        void shouldNoDispatchDeleteMessage_whenMessageNotDeleteStatus() {
            NextHearingDateVariables variables = NextHearingDateVariables.builder()
                .caseId(Long.parseLong(CASE_ID))
                .hearingId(HEARING_ID)
                .nextHearingDate(LocalDateTime.now())
                .build();

            when(objectMapper.convertValue(any(), eq(NextHearingDateVariables.class))).thenReturn(variables);
            handler.handleTask(mockTask);

            verifyNoInteractions(runtimeService);
            verifyNoInteractions(messageCorrelationBuilder);
            verifyNoInteractions(coreCaseDataService);
        }
    }
}
