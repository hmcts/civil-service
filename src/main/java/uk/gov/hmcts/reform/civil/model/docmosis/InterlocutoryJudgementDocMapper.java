package uk.gov.hmcts.reform.civil.model.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseDataParent;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.service.citizen.repaymentplan.RepaymentPlanDecisionCalculator;
import uk.gov.hmcts.reform.civil.service.docmosis.utils.ClaimantResponseUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InterlocutoryJudgementDocMapper implements MappableObject {
    private static final String REFER_TO_JUDGE = "Refer to Judge";

    private final RepaymentPlanDecisionCalculator repaymentPlanDecisionCalculator;
    public InterlocutoryJudgementDoc toInterlocutoryJudgementDoc(CaseData caseData) {
       return InterlocutoryJudgementDoc.builder()
            .claimNumber(caseData.getLegacyCaseReference())
            .claimIssueDate(caseData.getIssueDate())
            .claimantResponseSubmitDate(caseData.getApplicant1ResponseDate())
            .disposableIncome(repaymentPlanDecisionCalculator.calculateDisposableIncome(caseData))
            .claimantResponseToDefendantAdmission(getClaimantResponseToDefendantAdmission(caseData))
            .claimantRequestRepaymentBy(ClaimantResponseUtils.getClaimantRepaymentOption(caseData))
            .claimantRequestRepaymentLastDateBy(getClaimantRequestRepaymentLastDateBy(caseData))
            .courtDecisionRepaymentBy(getCourtDecisionPaymentBy(caseData))
           . courtDecisionRepaymentLastDateBy(getCourtDecisionRepaymentLastDateBy(caseData))
            .formalisePaymentBy(REFER_TO_JUDGE)
            .build();

    }

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

    private LocalDate getClaimantRequestRepaymentLastDateBy(CaseData caseData) {
        PaymentType claimantRepaymentOption = caseData.getApplicant1RepaymentOptionForDefendantSpec();
        if(claimantRepaymentOption == PaymentType.REPAYMENT_PLAN){
            return ClaimantResponseUtils.getClaimantFinalRepaymentDate(caseData);
        } else if(claimantRepaymentOption == PaymentType.SET_DATE) {
                return LocalDate.now();
        }
        return null;
    }

    private LocalDate getCourtDecisionRepaymentLastDateBy(CaseData caseData) {
        RepaymentDecisionType repaymentDecisionType = Optional.ofNullable(caseData).map(CaseDataParent::getCaseDataLiP)
            .map(CaseDataLiP::getCourtDecision).orElse(null);

        if (repaymentDecisionType == null) {
            return null;
        }
        switch (repaymentDecisionType) {
            case IN_FAVOUR_OF_CLAIMANT -> {
                return getClaimantRequestRepaymentLastDateBy(caseData);
            }
            case IN_FAVOUR_OF_DEFENDANT -> {
                return getDefendantRepaymentLastDateBy(caseData);
            }

        };
        return null;
    }

    private String getCourtDecisionPaymentBy(CaseData caseData) {
        RepaymentDecisionType repaymentDecisionType = Optional.ofNullable(caseData).map(CaseDataParent::getCaseDataLiP)
            .map(CaseDataLiP::getCourtDecision).orElse(null);

        if (repaymentDecisionType == null) {
            return "No repayment decision";
        }
        switch (repaymentDecisionType) {
            case IN_FAVOUR_OF_CLAIMANT -> {
                return ClaimantResponseUtils.getClaimantRepaymentOption(caseData);
            }
            case IN_FAVOUR_OF_DEFENDANT -> {
                return ClaimantResponseUtils.getDefendantRepaymentOption(caseData);
            }

        };
        return null;
    }

    private LocalDate getDefendantRepaymentLastDateBy(CaseData caseData) {

        RespondentResponsePartAdmissionPaymentTimeLRspec defendantPaymentOption = caseData.getDefenceAdmitPartPaymentTimeRouteRequired();
        if(defendantPaymentOption == RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN){
            return ClaimantResponseUtils.getDefendantFinalRepaymentDate(caseData);
        } else if(defendantPaymentOption == RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE) {
            return Optional.ofNullable(caseData.getRespondToClaimAdmitPartLRspec())
               .map(RespondToClaimAdmitPartLRspec::getWhenWillThisAmountBePaid).orElse(null);
        }

        return null;
    }
}
