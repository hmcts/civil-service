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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class PaymentTimeRouteCaseDataUpdaterTest {

    @Mock
    private DeadlineExtensionCalculatorService deadlineCalculatorService;

    @InjectMocks
    private PaymentTimeRouteCaseDataUpdater updater;

    @Test
    void shouldUpdateWhenRespondent1IsPartAdmissionImmediate() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData.setRespondent1ClaimResponseTypeForSpec(PART_ADMISSION);

        LocalDate expectedDate = stubImmediateDeadline();
        updater.update(caseData);

        assertThat(caseData.getRespondToClaimAdmitPartLRspec()).isNotNull();
        assertThat(caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid()).isEqualTo(expectedDate);
        assertThat(caseData.getRespondToClaimAdmitPartLRspec2()).isNull();
    }

    @Test
    void shouldUpdateWhenRespondent2IsPartAdmissionImmediate() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setIsRespondent2(YES);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(IMMEDIATELY);
        caseData.setRespondent2ClaimResponseTypeForSpec(PART_ADMISSION);

        RespondToClaimAdmitPartLRspec existingR1 = new RespondToClaimAdmitPartLRspec(LocalDate.now().plusDays(1));
        caseData.setRespondToClaimAdmitPartLRspec(existingR1);

        LocalDate expectedDate = stubImmediateDeadline();
        updater.update(caseData);

        assertThat(caseData.getRespondToClaimAdmitPartLRspec2()).isNotNull();
        assertThat(caseData.getRespondToClaimAdmitPartLRspec2().getWhenWillThisAmountBePaid()).isEqualTo(expectedDate);
        assertThat(caseData.getRespondToClaimAdmitPartLRspec()).isSameAs(existingR1);
    }

    @Test
    void shouldUpdateWhenRespondent1IsFullAdmission() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData.setRespondent1ClaimResponseTypeForSpec(FULL_ADMISSION);

        LocalDate expectedDate = stubImmediateDeadline();
        updater.update(caseData);

        assertThat(caseData.getRespondToClaimAdmitPartLRspec()).isNotNull();
        assertThat(caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid()).isEqualTo(expectedDate);
        assertThat(caseData.getRespondToClaimAdmitPartLRspec2()).isNull();
    }

    @Test
    void shouldUpdateRespondent2AdmitPartWhenRespondent2IsFullAdmissionImmediate() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setIsRespondent2(YES);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(IMMEDIATELY);
        caseData.setRespondent2ClaimResponseTypeForSpec(FULL_ADMISSION);

        RespondToClaimAdmitPartLRspec existingR1 = new RespondToClaimAdmitPartLRspec(LocalDate.now().plusDays(1));
        caseData.setRespondToClaimAdmitPartLRspec(existingR1);

        LocalDate expectedDate = stubImmediateDeadline();
        updater.update(caseData);

        assertThat(caseData.getRespondToClaimAdmitPartLRspec2()).isNotNull();
        assertThat(caseData.getRespondToClaimAdmitPartLRspec2().getWhenWillThisAmountBePaid()).isEqualTo(expectedDate);
        assertThat(caseData.getRespondToClaimAdmitPartLRspec()).isSameAs(existingR1);
    }

    @Test
    void shouldNotOverwriteRespondent1WhenRespondent2FullAdmissionButCurrentIsRespondent1() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData.setRespondent2ClaimResponseTypeForSpec(FULL_ADMISSION);
        caseData.setRespondent1ClaimResponseTypeForSpec(null);

        updater.update(caseData);

        assertThat(caseData.getRespondToClaimAdmitPartLRspec()).isNull();
        assertThat(caseData.getRespondToClaimAdmitPartLRspec2()).isNull();
    }

    @Test
    void shouldNotUpdateWhenNeitherIsAdmission() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData.setRespondent1ClaimResponseTypeForSpec(null);
        caseData.setRespondent2ClaimResponseTypeForSpec(null);

        updater.update(caseData);

        assertThat(caseData.getRespondToClaimAdmitPartLRspec()).isNull();
        assertThat(caseData.getRespondToClaimAdmitPartLRspec2()).isNull();
    }

    private LocalDate stubImmediateDeadline() {
        LocalDate expectedDate = LocalDate.now().plusDays(
            RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY
        );
        doReturn(expectedDate).when(deadlineCalculatorService)
            .calculateExtendedDeadline(any(LocalDateTime.class), anyInt());
        return expectedDate;
    }
}
