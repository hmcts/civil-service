package uk.gov.hmcts.reform.civil.model.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.service.citizen.repaymentplan.RepaymentPlanDecisionCalculator;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.service.docmosis.utils.ClaimantResponseUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.service.docmosis.utils.ClaimantResponseUtils.getClaimantSuggestedRepaymentType;
import static uk.gov.hmcts.reform.civil.service.docmosis.utils.ClaimantResponseUtils.getDefendantRepaymentOption;

@Component
@RequiredArgsConstructor
public class InterlocutoryJudgementDocMapper implements MappableObject {

    private static final String REFER_TO_JUDGE = "Refer to Judge";

    private final DeadlineExtensionCalculatorService calculatorService;
    private final RepaymentPlanDecisionCalculator repaymentPlanDecisionCalculator;

    private static String getClaimantResponseToDefendantAdmission(CaseData caseData) {
        RespondentResponseTypeSpec respondentResponseTypeSpec = caseData.getRespondent1ClaimResponseTypeForSpec();

        if (respondentResponseTypeSpec == null) {
            return "No respondent response type";
        }
        return switch (respondentResponseTypeSpec) {
            case PART_ADMISSION -> "I accept part admission";
            case FULL_ADMISSION -> "I accept full admission";
            default -> "";
        };
    }

    public InterlocutoryJudgementDoc toInterlocutoryJudgementDoc(CaseData caseData) {
        return InterlocutoryJudgementDoc.builder()
            .claimIssueDate(caseData.getIssueDate())
            .claimNumber(caseData.getLegacyCaseReference())
            .claimantRequestRepaymentBy(getClaimantSuggestedRepaymentType(caseData))
            .claimantRequestRepaymentLastDateBy(getClaimantRequestRepaymentLastDateBy(caseData))
            .claimantResponseSubmitDateTime(caseData.getApplicant1ResponseDate())
            .claimantResponseToDefendantAdmission(getClaimantResponseToDefendantAdmission(caseData))
            .courtDecisionRepaymentBy(getDefendantRepaymentOption(caseData))
            .courtDecisionRepaymentLastDateBy(getDefendantRepaymentLastDateBy(caseData))
            .formalisePaymentBy(REFER_TO_JUDGE)
            .formattedDisposableIncome(getFormattedDisposableIncome(caseData))
            .rejectionReason(getApplicant1RejectedRepaymentReason(caseData))
            .build();

    }

    private LocalDate getClaimantRequestRepaymentLastDateBy(CaseData caseData) {
        PaymentType claimantRepaymentOption = caseData.getApplicant1RepaymentOptionForDefendantSpec();
        if (claimantRepaymentOption == PaymentType.REPAYMENT_PLAN) {
            return ClaimantResponseUtils.getClaimantFinalRepaymentDate(caseData);
        } else if (claimantRepaymentOption == PaymentType.SET_DATE) {
            var paymentDate = caseData.getApplicant1RequestedPaymentDateForDefendantSpec();
            return paymentDate != null ? paymentDate.getPaymentSetDate() : null;
        } else if (claimantRepaymentOption == PaymentType.IMMEDIATELY) {
            return calculatorService.calculateExtendedDeadline(LocalDate.now(), 5);
        }
        return null;
    }

    private LocalDate getDefendantRepaymentLastDateBy(CaseData caseData) {

        RespondentResponsePartAdmissionPaymentTimeLRspec defendantPaymentOption = caseData.getDefenceAdmitPartPaymentTimeRouteRequired();
        if (defendantPaymentOption == RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN) {
            return ClaimantResponseUtils.getDefendantFinalRepaymentDate(caseData);
        } else if (defendantPaymentOption == RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE) {
            return Optional.ofNullable(caseData.getRespondToClaimAdmitPartLRspec())
                .map(RespondToClaimAdmitPartLRspec::getWhenWillThisAmountBePaid).orElse(null);
        } else if (defendantPaymentOption == RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY) {
            return calculatorService.calculateExtendedDeadline(LocalDate.now(), 5);
        }

        return null;
    }

    private String getFormattedDisposableIncome(CaseData caseData) {
        StringBuilder defendantDisposableIncome = new StringBuilder();

        BigDecimal disposableIncome = BigDecimal.valueOf(repaymentPlanDecisionCalculator.calculateDisposableIncome(
            caseData)).setScale(2, RoundingMode.CEILING);
        if (disposableIncome.compareTo(BigDecimal.ZERO) < 0) {
            defendantDisposableIncome.append("-");
        }

        defendantDisposableIncome.append("£");
        return defendantDisposableIncome.append(disposableIncome.abs()).toString();
    }

    private String getApplicant1RejectedRepaymentReason(CaseData caseData) {
        return Optional.ofNullable(caseData.getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .map(ClaimantLiPResponse::getApplicant1RejectedRepaymentReason)
            .orElse(null);
    }

}
