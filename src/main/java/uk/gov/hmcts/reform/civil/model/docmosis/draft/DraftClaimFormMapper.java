package uk.gov.hmcts.reform.civil.model.docmosis.draft;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.lip.LipFormParty;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE;

@Component
@RequiredArgsConstructor
public class DraftClaimFormMapper {

    private static final String STANDARD_INTEREST_RATE = "8";
    private static final String EXPLANATION_OF_INTEREST_RATE = "The claimant reserves the right to claim interest under "
        + "Section 69 of the County Courts Act 1984";
    private final InterestCalculator interestCalculator;

    public DraftClaimForm toDraftClaimForm(CaseData caseData) {
        BigDecimal interest = interestCalculator.calculateInterest(caseData);
        return DraftClaimForm.builder()
            .totalInterestAmount(interest != null ? interest.toString() : null)
            .howTheInterestWasCalculated(Optional.ofNullable(caseData.getInterestClaimOptions()).map(
                InterestClaimOptions::getDescription).orElse(null))
            .interestRate(caseData.getSameRateInterestSelection() != null ?
                              Optional.ofNullable(caseData.getSameRateInterestSelection().getDifferentRate())
                                  .map(BigDecimal::toString)
                              .orElse(STANDARD_INTEREST_RATE) : null)
            .interestExplanationText(caseData.getSameRateInterestSelection() != null
                                         ? caseData.getSameRateInterestSelection().getDifferentRate() != null
                ? caseData.getSameRateInterestSelection().getDifferentRateReason()
                : EXPLANATION_OF_INTEREST_RATE : null)
            .interestFromDate(Optional.ofNullable(caseData.getInterestFromSpecificDate())
                                  .orElse(Optional.ofNullable(caseData.getSubmittedDate())
                                              .map(LocalDateTime::toLocalDate).orElse(null)))
            .whenAreYouClaimingInterestFrom(caseData.getInterestClaimFrom() != null
                                                ? caseData.getInterestClaimFrom().equals(FROM_CLAIM_SUBMIT_DATE)
                ? "From the date the claim was issued"
                : caseData.getInterestFromSpecificDateDescription() : null)
            .totalClaimAmount(caseData.getTotalClaimAmount().toString())
            .interestAmount(interest != null ? interest.toString() : null)
            .claimAmount(caseData.getClaimAmountBreakupDetails())
            .claimFee(MonetaryConversions.penniesToPounds(caseData.getCalculatedClaimFeeInPence())
                          .toString())
            // Claim amount + interest + claim fees
            .totalAmountOfClaim(interest != null ? caseData.getTotalClaimAmount()
                .add(interest)
                .add(MonetaryConversions.penniesToPounds(caseData.getCalculatedClaimFeeInPence())).toString()
                                    : Optional.ofNullable(caseData.getTotalClaimAmount()).orElse(BigDecimal.ZERO)
                .add(MonetaryConversions.penniesToPounds(caseData.getCalculatedClaimFeeInPence())).toString())
            .descriptionOfClaim(caseData.getDetailsOfClaim())
            .claimant(LipFormParty.toLipFormParty(caseData.getApplicant1(), null, null))
            .defendant(LipFormParty.toLipFormParty(caseData.getRespondent1(), null, null))
            .generationDate(LocalDate.now())
            .build();
    }
}
