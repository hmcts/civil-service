package uk.gov.hmcts.reform.civil.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.hmc.model.messaging.HearingUpdate;
import uk.gov.hmcts.reform.hmc.model.messaging.HmcMessage;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REVIEW_HEARING_EXCEPTION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_NEXT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.ADJOURNED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.AWAITING_ACTUALS;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.CANCELLED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.COMPLETED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.EXCEPTION;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.HEARING_REQUESTED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.LISTED;

@ExtendWith(MockitoExtension.class)
class HmcMessageHandlerTest {

    @InjectMocks
    private HmcMessageHandler handler;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private PaymentsConfiguration paymentsConfiguration;

    @BeforeEach
    void setUp() {
        when(paymentsConfiguration.getSpecSiteId()).thenReturn("AAA6");
    }

    @Test
    void shouldNotTriggerEvent_whenMessageRelevantForServiceAndHearingException() {
        when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");

        HmcMessage hmcMessage = HmcMessage.builder()
            .caseId(1234L)
            .hearingId("HER1234")
            .hmctsServiceCode("AAA7")
            .hearingUpdate(HearingUpdate.builder()
                               .hmcStatus(EXCEPTION)
                               .build())
            .build();

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, REVIEW_HEARING_EXCEPTION);
    }

    @Test
    void shouldNotTriggerEvent_whenMessageRelevantForServiceNoHearingException() {
        when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");

        HmcMessage hmcMessage = HmcMessage.builder()
            .caseId(1234L)
            .hearingId("HER1234")
            .hmctsServiceCode("AAA7")
            .hearingUpdate(HearingUpdate.builder()
                               .hmcStatus(HEARING_REQUESTED)
                               .build())
            .build();

        handler.handleMessage(hmcMessage);

        verifyNoInteractions(coreCaseDataService);
    }

    @Test
    void shouldTriggerEvent_whenServiceIdIsAAA6AndHMCStatusIsListed() {
        HmcMessage hmcMessage = HmcMessage.builder()
                .caseId(1234L)
                .hearingId("HER1234")
                .hmctsServiceCode("AAA6")
                .hearingUpdate(HearingUpdate.builder()
                        .hmcStatus(LISTED)
                        .build())
                .build();

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, UPDATE_NEXT_HEARING_DETAILS);
    }

    @Test
    void shouldTriggerEvent_whenServiceIdIsAAA7AndHMCStatusIsListed() {
        when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");

        HmcMessage hmcMessage = HmcMessage.builder()
                .caseId(1234L)
                .hearingId("HER1234")
                .hmctsServiceCode("AAA7")
                .hearingUpdate(HearingUpdate.builder()
                        .hmcStatus(LISTED)
                        .build())
                .build();

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, UPDATE_NEXT_HEARING_DETAILS);
    }

    @Test
    void shouldTriggerEvent_whenServiceIdIsAAA6AndHMCStatusIsAwaitingActuals() {
        HmcMessage hmcMessage = HmcMessage.builder()
                .caseId(1234L)
                .hearingId("HER1234")
                .hmctsServiceCode("AAA6")
                .hearingUpdate(HearingUpdate.builder()
                        .hmcStatus(AWAITING_ACTUALS)
                        .build())
                .build();

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, UPDATE_NEXT_HEARING_DETAILS);
    }

    @Test
    void shouldTriggerEvent_whenServiceIdIsAAA7AndHMCStatusIsAwaitingActuals() {
        when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");

        HmcMessage hmcMessage = HmcMessage.builder()
                .caseId(1234L)
                .hearingId("HER1234")
                .hmctsServiceCode("AAA7")
                .hearingUpdate(HearingUpdate.builder()
                        .hmcStatus(AWAITING_ACTUALS)
                        .build())
                .build();

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, UPDATE_NEXT_HEARING_DETAILS);
    }

    @Test
    void shouldTriggerEvent_whenServiceIdIsAAA6AndHMCStatusIsCompleted() {
        HmcMessage hmcMessage = HmcMessage.builder()
                .caseId(1234L)
                .hearingId("HER1234")
                .hmctsServiceCode("AAA6")
                .hearingUpdate(HearingUpdate.builder()
                        .hmcStatus(COMPLETED)
                        .build())
                .build();

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, UPDATE_NEXT_HEARING_DETAILS);
    }

    @Test
    void shouldTriggerEvent_whenServiceIdIsAAA7AndHMCStatusIsCompleted() {
        when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");

        HmcMessage hmcMessage = HmcMessage.builder()
                .caseId(1234L)
                .hearingId("HER1234")
                .hmctsServiceCode("AAA7")
                .hearingUpdate(HearingUpdate.builder()
                        .hmcStatus(COMPLETED)
                        .build())
                .build();

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, UPDATE_NEXT_HEARING_DETAILS);
    }

    @Test
    void shouldTriggerEvent_whenServiceIdIsAAA6AndHMCStatusIsCenceled() {
        HmcMessage hmcMessage = HmcMessage.builder()
                .caseId(1234L)
                .hearingId("HER1234")
                .hmctsServiceCode("AAA6")
                .hearingUpdate(HearingUpdate.builder()
                        .hmcStatus(CANCELLED)
                        .build())
                .build();

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, UPDATE_NEXT_HEARING_DETAILS);
    }

    @Test
    void shouldTriggerEvent_whenServiceIdIsAAA7AndHMCStatusIsCanceled() {
        when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");

        HmcMessage hmcMessage = HmcMessage.builder()
                .caseId(1234L)
                .hearingId("HER1234")
                .hmctsServiceCode("AAA7")
                .hearingUpdate(HearingUpdate.builder()
                        .hmcStatus(CANCELLED)
                        .build())
                .build();

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, UPDATE_NEXT_HEARING_DETAILS);
    }

    @Test
    void shouldTriggerEvent_whenServiceIdIsAAA6AndHMCStatusIsAdjourned() {
        HmcMessage hmcMessage = HmcMessage.builder()
                .caseId(1234L)
                .hearingId("HER1234")
                .hmctsServiceCode("AAA6")
                .hearingUpdate(HearingUpdate.builder()
                        .hmcStatus(ADJOURNED)
                        .build())
                .build();

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, UPDATE_NEXT_HEARING_DETAILS);
    }

    @Test
    void shouldTriggerEvent_whenServiceIdIsAAA7AndHMCStatusIsAdjourned() {
        when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");

        HmcMessage hmcMessage = HmcMessage.builder()
                .caseId(1234L)
                .hearingId("HER1234")
                .hmctsServiceCode("AAA7")
                .hearingUpdate(HearingUpdate.builder()
                        .hmcStatus(ADJOURNED)
                        .build())
                .build();

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, UPDATE_NEXT_HEARING_DETAILS);
    }

    @Test
    void shouldNotTriggerEvent_whenMessageServiceIdNotRelevant() {
        when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");

        HmcMessage hmcMessage = HmcMessage.builder()
            .caseId(1234L)
            .hearingId("HER1234")
            .hmctsServiceCode("AAA8")
            .hearingUpdate(HearingUpdate.builder()
                               .hmcStatus(EXCEPTION)
                               .build())
            .build();

        handler.handleMessage(hmcMessage);

        verifyNoInteractions(coreCaseDataService);
    }
}
