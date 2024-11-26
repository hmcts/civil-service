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
import static org.mockito.ArgumentMatchers.any;
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
    void shouldUpdateCaseDataWhenPaymentTimeIsImmediately() {
        LocalDate expectedDate = LocalDate.now().plusDays(RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY);
        when(deadlineCalculatorService.calculateExtendedDeadline(any(LocalDate.class), any(Integer.class)))
                .thenReturn(expectedDate);

        CaseData caseData = CaseData.builder()
                .defenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY)
                .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
                .build();

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();
        updater.update(caseData, updatedData);

        RespondToClaimAdmitPartLRspec admitPartLRspec = updatedData.build().getRespondToClaimAdmitPartLRspec();
        assertThat(admitPartLRspec).isNotNull();
        assertThat(admitPartLRspec.getWhenWillThisAmountBePaid()).isEqualTo(expectedDate);
    }
}