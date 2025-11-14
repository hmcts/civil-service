package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinetests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec.PaymentTimeRouteCaseDataUpdater;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;

@ExtendWith(MockitoExtension.class)
class PaymentTimeRouteCaseDataUpdaterTest {

    @Mock
    private DeadlineExtensionCalculatorService deadlineCalculatorService;

    @InjectMocks
    private PaymentTimeRouteCaseDataUpdater updater;

    @Test
    void shouldNotUpdateCaseDataWhenPartPaymentPaidImmediately() {
        // Given
        CaseData caseData = CaseData.builder().build();
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData.setRespondent1ClaimResponseTypeForSpec(PART_ADMISSION);

        // When
        updater.update(caseData);

        // Then
        RespondToClaimAdmitPartLRspec admitPartLRspec = caseData.getRespondToClaimAdmitPartLRspec();
        assertThat(admitPartLRspec).isNull();
    }

    @Test
    void shouldNotUpdateWhenRespondent2IsPartAdmission() {
        // Given
        CaseData caseData = CaseData.builder().build();
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData.setRespondent2ClaimResponseTypeForSpec(PART_ADMISSION);

        // When
        updater.update(caseData);

        // Then
        RespondToClaimAdmitPartLRspec admitPartLRspec = caseData.getRespondToClaimAdmitPartLRspec();
        assertThat(admitPartLRspec).isNull();
    }

    @Test
    void shouldUpdateWhenRespondent1IsFullAdmission() {
        // Given
        LocalDate expectedDate = LocalDate.now().plusDays(
            RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY
        );
        when(deadlineCalculatorService.calculateExtendedDeadline(
            org.mockito.ArgumentMatchers.any(java.time.LocalDateTime.class),
            anyInt()
        )).thenReturn(expectedDate);

        CaseData caseData = CaseData.builder().build();
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData.setRespondent1ClaimResponseTypeForSpec(
            uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION
        );

        // When
        updater.update(caseData);

        // Then
        RespondToClaimAdmitPartLRspec admitPartLRspec = caseData.getRespondToClaimAdmitPartLRspec();
        assertThat(admitPartLRspec).isNotNull();
        assertThat(admitPartLRspec.getWhenWillThisAmountBePaid()).isEqualTo(expectedDate);
    }

    @Test
    void shouldUpdateWhenRespondent2IsFullAdmission() {
        // Given
        LocalDate expectedDate = LocalDate.now().plusDays(
            RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY
        );
        when(deadlineCalculatorService.calculateExtendedDeadline(
            org.mockito.ArgumentMatchers.any(java.time.LocalDateTime.class),
            anyInt()
        )).thenReturn(expectedDate);

        CaseData caseData = CaseData.builder().build();
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData.setRespondent2ClaimResponseTypeForSpec(
            uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION
        );

        // When
        updater.update(caseData);

        // Then
        RespondToClaimAdmitPartLRspec admitPartLRspec = caseData.getRespondToClaimAdmitPartLRspec();
        assertThat(admitPartLRspec).isNotNull();
        assertThat(admitPartLRspec.getWhenWillThisAmountBePaid()).isEqualTo(expectedDate);
    }

    @Test
    void shouldNotUpdateWhenNeitherIsAdmission() {
        // Given
        CaseData caseData = CaseData.builder().build();
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData.setRespondent1ClaimResponseTypeForSpec(null);
        caseData.setRespondent2ClaimResponseTypeForSpec(null);

        // When
        updater.update(caseData);

        // Then
        RespondToClaimAdmitPartLRspec admitPartLRspec = caseData.getRespondToClaimAdmitPartLRspec();
        assertThat(admitPartLRspec).isNull();
    }
}
