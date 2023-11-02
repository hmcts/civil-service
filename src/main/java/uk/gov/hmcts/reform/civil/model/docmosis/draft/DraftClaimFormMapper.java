package uk.gov.hmcts.reform.civil.model.docmosis.draft;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.AdditionalLipPartyDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EventTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.lip.LipFormParty;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE;
import static uk.gov.hmcts.reform.civil.utils.DateUtils.getRequiredDateBeforeFourPm;

@Component
@RequiredArgsConstructor
public class DraftClaimFormMapper {

    private static final String STANDARD_INTEREST_RATE = "8";
    public static final String EXPLANATION_OF_INTEREST_RATE = "The claimant reserves the right to claim interest under "
        + "Section 69 of the County Courts Act 1984";
    public static final String INTEREST_START_FROM_CLAIM_ISSUED_DATE = "From the date the claim was issued";
    private final InterestCalculator interestCalculator;

    public DraftClaimForm toDraftClaimForm(CaseData caseData) {
        BigDecimal interest = interestCalculator.calculateInterest(caseData);
        Optional<CaseDataLiP> caseDataLip = Optional.ofNullable(caseData.getCaseDataLiP());
        Optional<AdditionalLipPartyDetails> applicantDetails =
            caseDataLip.map(CaseDataLiP::getApplicant1AdditionalLipPartyDetails);
        Optional<AdditionalLipPartyDetails> defendantDetails =
            caseDataLip.map(CaseDataLiP::getRespondent1AdditionalLipPartyDetails);

        return DraftClaimForm.builder()
            .totalInterestAmount(interest != null ? interest.toString() : null)
            .howTheInterestWasCalculated(Optional.ofNullable(caseData.getInterestClaimOptions()).map(
                InterestClaimOptions::getDescription).orElse(null))
            .interestRate(getInterestRate(caseData))
            .interestExplanationText(generateInterestRateExplanation(caseData))
            .interestFromDate(getInterestFromDate(caseData))
            .interestEndDate(getInterestEndDate(caseData))
            .interestEndDateDescription(Optional.ofNullable(caseData.getBreakDownInterestDescription())
                                            .orElse(null))
            .whenAreYouClaimingInterestFrom(generateWhenAreYouPlanningInterestFrom(caseData))
            .timelineEvents(EventTemplateData.toEventTemplateDataList(caseData.getTimelineOfEvents()))
            .totalClaimAmount(Optional.ofNullable(caseData.getTotalClaimAmount())
                                  .map(BigDecimal::toString)
                                  .orElse("0"))
            .interestAmount(interest != null ? interest.toString() : null)
            .claimAmount(caseData.getClaimAmountBreakupDetails())
            .claimFee(MonetaryConversions.penniesToPounds(caseData.getCalculatedClaimFeeInPence())
                          .toString())
            .totalAmountOfClaim(calculateTotalAmountOfClaim(caseData, interest))
            .descriptionOfClaim(caseData.getDetailsOfClaim())
            .claimant(LipFormParty.toLipFormParty(
                caseData.getApplicant1(),
                getCorrespondenceAddress(applicantDetails),
                getContactPerson(applicantDetails)
            ))
            .defendant(LipFormParty.toLipFormParty(
                caseData.getRespondent1(),
                getCorrespondenceAddress(defendantDetails),
                getContactPerson(defendantDetails)
            ))
            .generationDate(LocalDate.now())
            .build();
    }

    @Nullable
    private static LocalDate getInterestEndDate(CaseData caseData) {
        return StringUtils.isBlank(caseData.getBreakDownInterestDescription())
            ? getRequiredDateBeforeFourPm(LocalDateTime.now())
            : null;
    }

    @Nullable
    private static LocalDate getInterestFromDate(CaseData caseData) {
        return Optional.ofNullable(caseData.getInterestFromSpecificDate())
            .orElse(Optional.ofNullable(caseData.getSubmittedDate())
                        .map(LocalDateTime::toLocalDate).orElse(null));
    }

    @Nullable
    private static String getInterestRate(CaseData caseData) {
        return caseData.getSameRateInterestSelection() != null
            ? Optional.ofNullable(caseData.getSameRateInterestSelection().getDifferentRate())
            .map(BigDecimal::toString)
            .orElse(STANDARD_INTEREST_RATE) : null;
    }

    private Address getCorrespondenceAddress(Optional<AdditionalLipPartyDetails> partyDetails) {
        return partyDetails.map(AdditionalLipPartyDetails::getCorrespondenceAddress).orElse(null);
    }

    private String getContactPerson(Optional<AdditionalLipPartyDetails> partyDetails) {
        return partyDetails.map(AdditionalLipPartyDetails::getContactPerson).orElse(null);
    }

    private String generateInterestRateExplanation(CaseData caseData) {
        return caseData.getSameRateInterestSelection() != null
            ? Optional.ofNullable(caseData.getSameRateInterestSelection().getDifferentRateReason())
            .orElse(EXPLANATION_OF_INTEREST_RATE)
            : null;
    }

    private String generateWhenAreYouPlanningInterestFrom(CaseData caseData) {
        return Optional.ofNullable(caseData.getInterestClaimFrom())
            .map(interestClaimFromType -> {
                if (interestClaimFromType.equals(FROM_CLAIM_SUBMIT_DATE)) {
                    return INTEREST_START_FROM_CLAIM_ISSUED_DATE;
                }
                return caseData.getInterestFromSpecificDateDescription();
            }).orElse(null);
    }

    private String calculateTotalAmountOfClaim(CaseData caseData, BigDecimal interest) {
        if (interest != null) {
            return Optional.ofNullable(caseData.getTotalClaimAmount()).orElse(BigDecimal.ZERO)
                .add(interest)
                .add(MonetaryConversions.penniesToPounds(caseData.getCalculatedClaimFeeInPence())).toString();
        }
        return Optional.ofNullable(caseData.getTotalClaimAmount()).orElse(BigDecimal.ZERO)
            .add(MonetaryConversions.penniesToPounds(caseData.getCalculatedClaimFeeInPence())).toString();
    }
}
