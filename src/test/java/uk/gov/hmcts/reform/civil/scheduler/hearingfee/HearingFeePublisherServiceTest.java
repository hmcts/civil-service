package uk.gov.hmcts.reform.civil.scheduler.hearingfee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.event.HearingFeePaidEvent;
import uk.gov.hmcts.reform.civil.event.HearingFeeUnpaidEvent;
import uk.gov.hmcts.reform.civil.event.NoHearingFeeDueEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.interceptor.TaskAbortedException;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingFeePublisherServiceTest {

    private static final Long CASE_ID = 123456789L;
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 21);

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private Time time;

    @InjectMocks
    private HearingFeePublisherService publisherService;

    @BeforeEach
    void setUp() {
        lenient().when(time.now()).thenReturn(LocalDateTime.of(2026, 9, 21, 12, 0));
    }

    @Test
    void shouldPublishNoHearingFeeDueEvent_whenMultiTrackAndHearingDueDateIsNull() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(CASE_ID)
            .ccdState(CaseState.HEARING_READINESS)
            .hearingDueDate(null)
            .build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);

        publisherService.publishHearingFeeEvent(caseData);

        ArgumentCaptor<NoHearingFeeDueEvent> captor = ArgumentCaptor.forClass(NoHearingFeeDueEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getCaseId()).isEqualTo(CASE_ID);
    }

    @Test
    void shouldPublishPaidEvent_whenMultiTrackAndHearingFeePaidSuccessfully() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(CASE_ID)
            .ccdState(CaseState.HEARING_READINESS)
            .hearingDueDate(TODAY.minusDays(1))
            .hearingFeePaymentDetails(new PaymentDetails().setStatus(PaymentStatus.SUCCESS))
            .build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);

        publisherService.publishHearingFeeEvent(caseData);

        ArgumentCaptor<HearingFeePaidEvent> captor = ArgumentCaptor.forClass(HearingFeePaidEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getCaseId()).isEqualTo(CASE_ID);
    }

    @Test
    void shouldPublishPaidEvent_whenPreMultiTrackAndHearingFeePaidSuccessfully() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(CASE_ID)
            .ccdState(CaseState.HEARING_READINESS)
            .hearingDueDate(TODAY.minusDays(1))
            .hearingFeePaymentDetails(new PaymentDetails().setStatus(PaymentStatus.SUCCESS))
            .build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(false);

        publisherService.publishHearingFeeEvent(caseData);

        ArgumentCaptor<HearingFeePaidEvent> captor = ArgumentCaptor.forClass(HearingFeePaidEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getCaseId()).isEqualTo(CASE_ID);
    }

    @Test
    void shouldPublishPaidEvent_whenPaidDoneWithHwfEvenIfDueDateInFuture() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(CASE_ID)
            .ccdState(CaseState.HEARING_READINESS)
            .hearingDueDate(TODAY.plusDays(5))
            .hwfFeeType(FeeType.HEARING)
            .hearingHelpFeesReferenceNumber("hwf-ref")
            .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails().setHwfFullRemissionGrantedForHearingFee(YesOrNo.YES))
            .build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(false);

        publisherService.publishHearingFeeEvent(caseData);

        ArgumentCaptor<HearingFeePaidEvent> captor = ArgumentCaptor.forClass(HearingFeePaidEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getCaseId()).isEqualTo(CASE_ID);
    }

    @Test
    void shouldPublishUnpaidEvent_whenDueDatePassedAndPaymentFailed() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(CASE_ID)
            .ccdState(CaseState.HEARING_READINESS)
            .hearingDueDate(TODAY.minusDays(1))
            .hearingFeePaymentDetails(new PaymentDetails().setStatus(PaymentStatus.FAILED))
            .build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);

        publisherService.publishHearingFeeEvent(caseData);

        ArgumentCaptor<HearingFeeUnpaidEvent> captor = ArgumentCaptor.forClass(HearingFeeUnpaidEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getCaseId()).isEqualTo(CASE_ID);
    }

    @Test
    void shouldPublishUnpaidEvent_whenPreMultiTrackAndDueDatePassedAndPaymentDetailsNull() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(CASE_ID)
            .ccdState(CaseState.HEARING_READINESS)
            .hearingDueDate(TODAY.minusDays(1))
            .hearingFeePaymentDetails(null)
            .build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(false);

        publisherService.publishHearingFeeEvent(caseData);

        ArgumentCaptor<HearingFeeUnpaidEvent> captor = ArgumentCaptor.forClass(HearingFeeUnpaidEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getCaseId()).isEqualTo(CASE_ID);
    }

    @Test
    void shouldThrowTaskAbortedException_whenDueDateIsInTheFuture() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(CASE_ID)
            .ccdState(CaseState.HEARING_READINESS)
            .hearingDueDate(TODAY.plusDays(3))
            .build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);

        assertThatThrownBy(() -> publisherService.publishHearingFeeEvent(caseData))
            .isInstanceOf(TaskAbortedException.class)
            .hasFieldOrPropertyWithValue("reason", "No condition matched (e.g. hearing due date not yet reached)");

        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldThrowTaskAbortedException_whenPreMultiTrackAndHearingDueDateIsNull() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(CASE_ID)
            .ccdState(CaseState.HEARING_READINESS)
            .hearingDueDate(null)
            .build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(false);

        assertThatThrownBy(() -> publisherService.publishHearingFeeEvent(caseData))
            .isInstanceOf(TaskAbortedException.class)
            .hasFieldOrPropertyWithValue("reason", "No condition matched (e.g. hearing due date not yet reached)");

        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldThrowTaskAbortedException_whenDueDateIsToday() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(CASE_ID)
            .ccdState(CaseState.HEARING_READINESS)
            .hearingDueDate(TODAY)
            .build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);

        assertThatThrownBy(() -> publisherService.publishHearingFeeEvent(caseData))
            .isInstanceOf(TaskAbortedException.class)
            .hasFieldOrPropertyWithValue("reason", "No condition matched (e.g. hearing due date not yet reached)");

        verifyNoInteractions(applicationEventPublisher);
    }
}
