package uk.gov.hmcts.reform.civil.model.docmosis.draft;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.docmosis.lip.LipFormParty;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DraftClaimFormMapper {

    private final InterestCalculator interestCalculator;

    public DraftClaimForm toDraftClaimForm(CaseData caseData) {
        BigDecimal interest = interestCalculator.calculateInterest(caseData);
        return DraftClaimForm.builder()
            .totalInterestAmount(interest != null ? interest.toString() : null)
            .howTheInterestWasCalculated(caseData.getInterestClaimOptions() != null
                                             ? caseData.getInterestClaimOptions().getDescription() : null)
            .interestRate(caseData.getSameRateInterestSelection() != null
                              ? caseData.getSameRateInterestSelection().getDifferentRate() != null
                ? caseData.getSameRateInterestSelection().getDifferentRate() + "" :
                "8" : null)
            .interestExplanationText(caseData.getSameRateInterestSelection() != null
                                         ? caseData.getSameRateInterestSelection().getDifferentRate() != null
                ? caseData.getSameRateInterestSelection().getDifferentRateReason()
                : "The claimant reserves the right to claim interest under "
                + "Section 69 of the County Courts Act 1984" : null)
            .interestFromDate(caseData.getInterestFromSpecificDate() != null
                                  ? caseData.getInterestFromSpecificDate() : caseData.getSubmittedDate().toLocalDate())
            .whenAreYouClaimingInterestFrom(caseData.getInterestClaimFrom() != null
                                                ? caseData.getInterestClaimFrom().name()
                .equals("FROM_CLAIM_SUBMIT_DATE")
                ? "From the date the claim was issued"
                : caseData.getInterestFromSpecificDateDescription() : null)
            .totalClaimAmount(caseData.getTotalClaimAmount() + "")
            .interestAmount(interest != null ? interest.toString() : null)
            .claimAmount(caseData.getClaimAmountBreakupDetails())
            .claimFee(MonetaryConversions.penniesToPounds(caseData.getCalculatedClaimFeeInPence())
                          .toString())
            // Claim amount + interest + claim fees
            .totalAmountOfClaim(interest != null ? caseData.getTotalClaimAmount()
                .add(interest)
                .add(MonetaryConversions.penniesToPounds(caseData.getCalculatedClaimFeeInPence())).toString()
                                    : caseData.getTotalClaimAmount()
                .add(MonetaryConversions.penniesToPounds(caseData.getCalculatedClaimFeeInPence())).toString())
            .descriptionOfClaim(caseData.getDetailsOfClaim())
            .claimant(LipFormParty.toLipFormParty(caseData.getApplicant1(), null, null))
            .defendant(LipFormParty.toLipFormParty(caseData.getRespondent1(), null, null))
            .build();
    }
}
