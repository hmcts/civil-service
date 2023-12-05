package uk.gov.hmcts.reform.civil.model.docmosis.claimantresponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.AdditionalLipPartyDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.docmosis.common.RepaymentPlanTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.lip.LipFormParty;
import uk.gov.hmcts.reform.civil.service.JudgementService;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JudgmentByAdmissionMapper {

    private final DeadlineExtensionCalculatorService deadlineCalculatorService;
    private final JudgementService judgementService;

    public JudgmentByAdmission toClaimantResponseForm(CaseData caseData) {
        Optional<CaseDataLiP> caseDataLip = Optional.ofNullable(caseData.getCaseDataLiP());
        Optional<AdditionalLipPartyDetails> applicantDetails =
            caseDataLip.map(CaseDataLiP::getApplicant1AdditionalLipPartyDetails);
        Optional<AdditionalLipPartyDetails> defendantDetails =
            caseDataLip.map(CaseDataLiP::getRespondent1AdditionalLipPartyDetails);
        caseData.getApplicant1().setPartyEmail(caseData.getClaimantUserDetails() != null
                                                   ? caseData.getClaimantUserDetails().getEmail() : null);
        LipFormParty claimant = LipFormParty.toLipFormParty(
            caseData.getApplicant1(),
            getCorrespondenceAddress(applicantDetails),
            getContactPerson(applicantDetails)
        );

        LipFormParty defendant = LipFormParty.toLipFormParty(
            caseData.getRespondent1(),
            getCorrespondenceAddress(defendantDetails),
            getContactPerson(defendantDetails)
        );

        String totalClaimAmount = Optional.ofNullable(caseData.getTotalClaimAmount())
            .map(BigDecimal::toString)
            .orElse("0");

        String totalInterest = judgementService.ccjJudgmentInterest(caseData).toString();


        JudgmentByAdmission.JudgmentByAdmissionBuilder builder = new JudgmentByAdmission.JudgmentByAdmissionBuilder();
        String formName = RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec()) ? "OCON225" : "OCON225a";
           return builder
            .formHeader("Judgment by admission")
            .formName(formName)
            .claimant(claimant)
            .defendant(defendant)
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .totalClaimAmount(totalClaimAmount)
            .totalInterestAmount(totalInterest)
            .paymentType(getPaymentType(caseData))
            .paymentTypeDisplayValue(getPaymentType(caseData).getDisplayedValue())
            .payBy(setPayByDate(caseData))
            .repaymentPlan(addRepaymentPlan(caseData))
            .ccjJudgmentAmount(judgementService.ccjJudgmentClaimAmount(caseData).toString())
            .ccjInterestToDate(totalInterest)
            .claimFee(judgementService.ccjJudgmentClaimFee(caseData).toString())
            .ccjSubtotal(judgementService.ccjJudgementSubTotal(caseData).toString())
            .ccjAlreadyPaidAmount(judgementService.ccjJudgmentPaidAmount(caseData).toString())
            .ccjFinalTotal(judgementService.ccjJudgmentFinalTotal(caseData).toString())
            .defendantResponse(caseData.getRespondent1ClaimResponseTypeForSpec())
            .generationDate(LocalDate.now())
            .build();
    }

    private LocalDate setPayByDate(CaseData caseData) {
        if (caseData.getApplicant1RepaymentOptionForDefendantSpec().equals(PaymentType.SET_DATE)) {
            return caseData.getApplicant1RequestedPaymentDateForDefendantSpec().getPaymentSetDate();
        } else if(caseData.getApplicant1RepaymentOptionForDefendantSpec().equals(PaymentType.IMMEDIATELY)) {
            LocalDate whenBePaid = deadlineCalculatorService.calculateExtendedDeadline(
                LocalDate.now(),
                RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY);
            return whenBePaid;
        }

        return null;
    }

    private ApplicantResponsePaymentPlan getPaymentType(CaseData caseData) {
        if(caseData.getApplicant1RepaymentOptionForDefendantSpec().equals(PaymentType.IMMEDIATELY)) {
            return ApplicantResponsePaymentPlan.IMMEDIATELY;
        } else if (caseData.getApplicant1RepaymentOptionForDefendantSpec().equals(PaymentType.SET_DATE)) {
            return ApplicantResponsePaymentPlan.SET_DATE;
        } else if (caseData.getApplicant1RepaymentOptionForDefendantSpec().equals(PaymentType.REPAYMENT_PLAN)) {
            return ApplicantResponsePaymentPlan.REPAYMENT_PLAN;
        }
        return null;
    }


    private Address getCorrespondenceAddress(Optional<AdditionalLipPartyDetails> partyDetails) {
        return partyDetails.map(AdditionalLipPartyDetails::getCorrespondenceAddress).orElse(null);
    }

    private String getContactPerson(Optional<AdditionalLipPartyDetails> partyDetails) {
        return partyDetails.map(AdditionalLipPartyDetails::getContactPerson).orElse(null);
    }

    private static RepaymentPlanTemplateData addRepaymentPlan(CaseData caseData) {
        RepaymentPlanTemplateData.RepaymentPlanTemplateDataBuilder builder = RepaymentPlanTemplateData.builder();
        if (caseData.getApplicant1RepaymentOptionForDefendantSpec().equals(PaymentType.REPAYMENT_PLAN)) {
            return builder
                .firstRepaymentDate(caseData.getApplicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec())
                .paymentAmount(caseData.getApplicant1SuggestInstalmentsPaymentAmountForDefendantSpec())
                .paymentFrequencyDisplay(caseData.getApplicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec().getLabel())
                .build();
        }
        return null;
    }

}
