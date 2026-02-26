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

        HmcMessage hmcMessage = new HmcMessage()
            .setCaseId(1234L)
            .setHearingId("HER1234")
            .setHmctsServiceCode("AAA7")
            .setHearingUpdate(new HearingUpdate()
                               .setHmcStatus(EXCEPTION));

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, REVIEW_HEARING_EXCEPTION);
    }

    @Test
    void shouldNotTriggerEvent_whenMessageRelevantForServiceNoHearingException() {
        when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");

        HmcMessage hmcMessage = new HmcMessage()
            .setCaseId(1234L)
            .setHearingId("HER1234")
            .setHmctsServiceCode("AAA7")
            .setHearingUpdate(new HearingUpdate()
                               .setHmcStatus(HEARING_REQUESTED));

        handler.handleMessage(hmcMessage);

        verifyNoInteractions(coreCaseDataService);
    }

    @Test
    void shouldTriggerEvent_whenServiceIdIsAAA6AndHMCStatusIsListed() {
        HmcMessage hmcMessage = new HmcMessage()
                .setCaseId(1234L)
                .setHearingId("HER1234")
                .setHmctsServiceCode("AAA6")
                .setHearingUpdate(new HearingUpdate()
                        .setHmcStatus(LISTED));

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, UPDATE_NEXT_HEARING_DETAILS);
    }

    @Test
    void shouldTriggerEvent_whenServiceIdIsAAA7AndHMCStatusIsListed() {
        when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");

        HmcMessage hmcMessage = new HmcMessage()
                .setCaseId(1234L)
                .setHearingId("HER1234")
                .setHmctsServiceCode("AAA7")
                .setHearingUpdate(new HearingUpdate()
                        .setHmcStatus(LISTED));

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, UPDATE_NEXT_HEARING_DETAILS);
    }

    @Test
    void shouldTriggerEvent_whenServiceIdIsAAA6AndHMCStatusIsAwaitingActuals() {
        HmcMessage hmcMessage = new HmcMessage()
                .setCaseId(1234L)
                .setHearingId("HER1234")
                .setHmctsServiceCode("AAA6")
                .setHearingUpdate(new HearingUpdate()
                        .setHmcStatus(AWAITING_ACTUALS));

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, UPDATE_NEXT_HEARING_DETAILS);
    }

    @Test
    void shouldTriggerEvent_whenServiceIdIsAAA7AndHMCStatusIsAwaitingActuals() {
        when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");

        HmcMessage hmcMessage = new HmcMessage()
                .setCaseId(1234L)
                .setHearingId("HER1234")
                .setHmctsServiceCode("AAA7")
                .setHearingUpdate(new HearingUpdate()
                        .setHmcStatus(AWAITING_ACTUALS));

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, UPDATE_NEXT_HEARING_DETAILS);
    }

    @Test
    void shouldTriggerEvent_whenServiceIdIsAAA6AndHMCStatusIsCompleted() {
        HmcMessage hmcMessage = new HmcMessage()
                .setCaseId(1234L)
                .setHearingId("HER1234")
                .setHmctsServiceCode("AAA6")
                .setHearingUpdate(new HearingUpdate()
                        .setHmcStatus(COMPLETED));

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, UPDATE_NEXT_HEARING_DETAILS);
    }

    @Test
    void shouldTriggerEvent_whenServiceIdIsAAA7AndHMCStatusIsCompleted() {
        when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");

        HmcMessage hmcMessage = new HmcMessage()
                .setCaseId(1234L)
                .setHearingId("HER1234")
                .setHmctsServiceCode("AAA7")
                .setHearingUpdate(new HearingUpdate()
                        .setHmcStatus(COMPLETED));

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, UPDATE_NEXT_HEARING_DETAILS);
    }

    @Test
    void shouldTriggerEvent_whenServiceIdIsAAA6AndHMCStatusIsCenceled() {
        HmcMessage hmcMessage = new HmcMessage()
                .setCaseId(1234L)
                .setHearingId("HER1234")
                .setHmctsServiceCode("AAA6")
                .setHearingUpdate(new HearingUpdate()
                        .setHmcStatus(CANCELLED));

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, UPDATE_NEXT_HEARING_DETAILS);
    }

    @Test
    void shouldTriggerEvent_whenServiceIdIsAAA7AndHMCStatusIsCanceled() {
        when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");

        HmcMessage hmcMessage = new HmcMessage()
                .setCaseId(1234L)
                .setHearingId("HER1234")
                .setHmctsServiceCode("AAA7")
                .setHearingUpdate(new HearingUpdate()
                        .setHmcStatus(CANCELLED));

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, UPDATE_NEXT_HEARING_DETAILS);
    }

    @Test
    void shouldTriggerEvent_whenServiceIdIsAAA6AndHMCStatusIsAdjourned() {
        HmcMessage hmcMessage = new HmcMessage()
                .setCaseId(1234L)
                .setHearingId("HER1234")
                .setHmctsServiceCode("AAA6")
                .setHearingUpdate(new HearingUpdate()
                        .setHmcStatus(ADJOURNED));

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, UPDATE_NEXT_HEARING_DETAILS);
    }

    @Test
    void shouldTriggerEvent_whenServiceIdIsAAA7AndHMCStatusIsAdjourned() {
        when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");

        HmcMessage hmcMessage = new HmcMessage()
                .setCaseId(1234L)
                .setHearingId("HER1234")
                .setHmctsServiceCode("AAA7")
                .setHearingUpdate(new HearingUpdate()
                        .setHmcStatus(ADJOURNED));

        handler.handleMessage(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, UPDATE_NEXT_HEARING_DETAILS);
    }

    @Test
    void shouldNotTriggerEvent_whenMessageServiceIdNotRelevant() {
        when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");

        HmcMessage hmcMessage = new HmcMessage()
            .setCaseId(1234L)
            .setHearingId("HER1234")
            .setHmctsServiceCode("AAA8")
            .setHearingUpdate(new HearingUpdate()
                               .setHmcStatus(EXCEPTION));

        handler.handleMessage(hmcMessage);

        verifyNoInteractions(coreCaseDataService);
    }
}
