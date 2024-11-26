package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;

@Component
@RequiredArgsConstructor
public class PaymentTimeRouteCaseDataUpdater implements SetApplicantResponseDeadlineCaseDataUpdater {

    private final DeadlineExtensionCalculatorService deadlineCalculatorService;

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (IMMEDIATELY.equals(caseData.getDefenceAdmitPartPaymentTimeRouteRequired())
                && ifResponseTypeIsPartOrFullAdmission(caseData)) {
            LocalDate whenBePaid = deadlineCalculatorService.calculateExtendedDeadline(
                    LocalDate.now(),
                    RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY);
            RespondToClaimAdmitPartLRspec admitPartLRspec = RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenBePaid)
                    .build();
            updatedData.respondToClaimAdmitPartLRspec(admitPartLRspec);
        }
    }

    private boolean ifResponseTypeIsPartOrFullAdmission(CaseData caseData) {
        return PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                || PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                || FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                || FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec());
    }
}
