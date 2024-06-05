package uk.gov.hmcts.reform.civil.model.docmosis.claimantresponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.AdditionalLipPartyDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.docmosis.common.RepaymentPlanTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.lip.LipFormParty;
import uk.gov.hmcts.reform.civil.service.JudgementService;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JudgmentByAdmissionOrDeterminationMapper {

    private final DeadlineExtensionCalculatorService deadlineCalculatorService;
    private final JudgementService judgementService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy 'at' h:mma");

    public JudgmentByAdmissionOrDetermination toClaimantResponseForm(CaseData caseData, CaseEvent caseEvent) {
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
            .map(amount -> amount.setScale(2).toString())
            .orElse("0");

        String totalInterest = judgementService.ccjJudgmentInterest(caseData).setScale(2).toString();

        JudgmentByAdmissionOrDetermination.JudgmentByAdmissionOrDeterminationBuilder builder = new JudgmentByAdmissionOrDetermination.JudgmentByAdmissionOrDeterminationBuilder();
        LocalDateTime now = LocalDateTime.now();
        return builder
            .formHeader(getFormHeader(caseData, caseEvent))
            .formName(getFormName(caseData))
            .claimant(claimant)
            .defendant(defendant)
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .totalClaimAmount(totalClaimAmount)
            .totalInterestAmount(totalInterest)
            .paymentType(getPaymentType(caseData))
            .paymentTypeDisplayValue(java.util.Objects.requireNonNull(getPaymentType(caseData)).getDisplayedValue())
            .payBy(setPayByDate(caseData))
            .repaymentPlan(addRepaymentPlan(caseData))
            .ccjJudgmentAmount(judgementService.ccjJudgmentClaimAmount(caseData).setScale(2).toString())
            .ccjInterestToDate(totalInterest)
            .claimFee(getClaimFee(caseData))
            .ccjSubtotal(judgementService.ccjJudgementSubTotal(caseData).setScale(2).toString())
            .ccjAlreadyPaidAmount(getAlreadyPaidAmount(caseData))
            .ccjFinalTotal(judgementService.ccjJudgmentFinalTotal(caseData).setScale(2).toString())
            .defendantResponse(caseData.getRespondent1ClaimResponseTypeForSpec())
            .generationDate(now.toLocalDate())
            .generationDateTime(now.format(formatter))
            .build();
    }

    private String getClaimFee(CaseData caseData) {
        BigDecimal claimFee = judgementService.ccjJudgmentClaimFee(caseData);
        if (BigDecimal.ZERO.compareTo(claimFee) == 0) {
            return BigDecimal.ZERO.toString();
        }
        return claimFee.setScale(2).toString();
    }

    private String getAlreadyPaidAmount(CaseData caseData) {
        BigDecimal paidAmount = judgementService.ccjJudgmentPaidAmount(caseData);
        if (BigDecimal.ZERO.compareTo(paidAmount) == 0) {
            return BigDecimal.ZERO.toString();
        }
        return paidAmount.setScale(2).toString();
    }

    private LocalDate setPayByDate(CaseData caseData) {
        if (caseData.getApplicant1RepaymentOptionForDefendantSpec().equals(PaymentType.SET_DATE)) {
            return caseData.getApplicant1RequestedPaymentDateForDefendantSpec().getPaymentSetDate();
        } else if (caseData.getApplicant1RepaymentOptionForDefendantSpec().equals(PaymentType.IMMEDIATELY)) {
            return deadlineCalculatorService.calculateExtendedDeadline(
                LocalDate.now(),
                RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY);
        }

        return null;
    }

    private ApplicantResponsePaymentPlan getPaymentType(CaseData caseData) {
        if (caseData.getApplicant1RepaymentOptionForDefendantSpec().equals(PaymentType.IMMEDIATELY)) {
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
                .paymentAmount(caseData.getApplicant1SuggestInstalmentsPaymentAmountForDefendantSpec().setScale(2))
                .paymentFrequencyDisplay(caseData.getApplicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec().getLabel())
                .build();
        }
        return null;
    }

    private String getFormHeader(CaseData caseData, CaseEvent caseEvent) {
        String formHeader = "Judgment by %s";
        String formType;
        if (YesOrNo.YES.equals(caseData.getApplicant1AcceptFullAdmitPaymentPlanSpec())
            || YesOrNo.YES.equals(caseData.getApplicant1AcceptPartAdmitPaymentPlanSpec())
            || caseEvent == CaseEvent.GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC) {
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
