package uk.gov.hmcts.reform.civil.model.docmosis.settlementagreement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.AdditionalLipPartyDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.docmosis.lip.LipFormParty;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;

@Component
@RequiredArgsConstructor
public class SettlementAgreementFormMapper {

    public SettlementAgreementForm buildFormData(CaseData caseData) {
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

        SettlementAgreementForm.SettlementAgreementFormBuilder builder = new SettlementAgreementForm.SettlementAgreementFormBuilder();
        return builder
                .claimant(claimant)
                .defendant(defendant)
                .claimReferenceNumber(caseData.getLegacyCaseReference())
                .totalClaimAmount(totalClaimAmount)
                .settlementAgreedDate(getSettlementDate(caseData))
                .settlementSubmittedDate(caseData.getRespondent1ResponseDate())
                .build();
    }

    private Address getCorrespondenceAddress(Optional<AdditionalLipPartyDetails> partyDetails) {
        return partyDetails.map(AdditionalLipPartyDetails::getCorrespondenceAddress).orElse(null);
    }

    private String getContactPerson(Optional<AdditionalLipPartyDetails> partyDetails) {
        return partyDetails.map(AdditionalLipPartyDetails::getContactPerson).orElse(null);
    }

    private LocalDate getSettlementDate(CaseData caseData) {
        if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == BY_SET_DATE) {
            return caseData.getDateForRepayment();

        }
        if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == SUGGESTION_OF_REPAYMENT_PLAN) {
            return getClaimantFinalRepaymentDate(caseData);
        }
        return null;
    }

    public static LocalDate getClaimantFinalRepaymentDate(CaseData caseData) {
        BigDecimal paymentAmount = caseData.getRespondent1RepaymentPlan().getPaymentAmount();
        BigDecimal paymentAmountPounds = MonetaryConversions.penniesToPounds(paymentAmount);
        LocalDate firstRepaymentDate = caseData.getRespondent1RepaymentPlan().getFirstRepaymentDate();
        PaymentFrequencyLRspec repaymentFrequency = caseData.getRespondent1RepaymentPlan().getRepaymentFrequency();

        BigDecimal claimantTotalAmount = caseData.getTotalClaimAmount();
        if (isNull(firstRepaymentDate) || isNull(paymentAmountPounds) || isNull(repaymentFrequency)) {
            return null;
        }
        long numberOfInstallmentsAfterFirst = getNumberOfInstallmentsAfterFirst(claimantTotalAmount, paymentAmountPounds);

        return switch (repaymentFrequency) {
            case ONCE_ONE_WEEK -> firstRepaymentDate.plusWeeks(numberOfInstallmentsAfterFirst);
            case ONCE_TWO_WEEKS -> firstRepaymentDate.plusWeeks(2 * numberOfInstallmentsAfterFirst);
            default -> firstRepaymentDate.plusMonths(numberOfInstallmentsAfterFirst);
        };
    }

    private static long getNumberOfInstallmentsAfterFirst(BigDecimal totalAmount, BigDecimal paymentAmount) {
        return totalAmount.divide(paymentAmount, 0, RoundingMode.CEILING).longValue() - 1;
    }
}
