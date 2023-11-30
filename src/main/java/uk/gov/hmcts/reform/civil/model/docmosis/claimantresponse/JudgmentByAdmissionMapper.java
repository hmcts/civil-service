package uk.gov.hmcts.reform.civil.model.docmosis.claimantresponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
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
        return builder
            .formHeader(getFormHeader(caseData))
            .formName(getFormName(caseData))
            .claimant(claimant)
            .defendant(defendant)
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .totalClaimAmount(totalClaimAmount)
            .totalInterestAmount(totalInterest)
            //.paymentType(getPaymentType(caseData))
            //.paymentTypeDisplayValue(getPaymentType(caseData).getDisplayedValue())
            //.payBy(setPayByDate(caseData))
            //.repaymentPlan(getRepaymentPlan(caseData))
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
        if (caseData.getPaymentTypeSelection().equals(DJPaymentTypeSelection.SET_DATE)) {
            return caseData.getPaymentSetDate();
        } else if(caseData.getPaymentTypeSelection().equals(DJPaymentTypeSelection.IMMEDIATELY)) {
            LocalDate whenBePaid = deadlineCalculatorService.calculateExtendedDeadline(
                LocalDate.now(),
                RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY);
            return whenBePaid;
        }

        return null;
    }

    private ApplicantResponsePaymentPlan getPaymentType(CaseData caseData) {
        if(caseData.getPaymentTypeSelection().equals(DJPaymentTypeSelection.IMMEDIATELY)) {
            return ApplicantResponsePaymentPlan.IMMEDIATELY;
        } else if (caseData.getPaymentTypeSelection().equals(DJPaymentTypeSelection.SET_DATE)) {
            return ApplicantResponsePaymentPlan.SET_DATE;
        } else if (caseData.getPaymentTypeSelection().equals(DJPaymentTypeSelection.REPAYMENT_PLAN)) {
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

    private static RepaymentPlanTemplateData getRepaymentPlan(CaseData caseData) {
        RepaymentPlanLRspec repaymentPlan = caseData.getRespondent1RepaymentPlan();
        if (repaymentPlan != null) {
            return RepaymentPlanTemplateData.builder()
                                      .paymentFrequencyDisplay(repaymentPlan.getPaymentFrequencyDisplay())
                                      .firstRepaymentDate(repaymentPlan.getFirstRepaymentDate())
                                      .paymentAmount(MonetaryConversions.penniesToPounds(repaymentPlan.getPaymentAmount()))
                                      .build();
        }
        return null;
    }

    private String getFormHeader(CaseData caseData) {
        String formHeader = "Judgment by %s";
        String formType;
        if (YesOrNo.YES.equals(caseData.getApplicant1AcceptFullAdmitPaymentPlanSpec())
            || YesOrNo.YES.equals(caseData.getApplicant1AcceptPartAdmitPaymentPlanSpec())) {
            formType = "admission";
        } else {
            formType = "determination";
        }
        return String.format(formHeader, formType);
    }

    private String getFormName(CaseData caseData) {
        return RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            ? "OCON225"
            : "OCON225a";
    }
}
