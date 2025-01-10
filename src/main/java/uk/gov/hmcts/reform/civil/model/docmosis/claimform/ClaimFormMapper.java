package uk.gov.hmcts.reform.civil.model.docmosis.claimform;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.FlightDelayDetails;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;
import uk.gov.hmcts.reform.civil.model.citizenui.AdditionalLipPartyDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EvidenceTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Timeline;
import uk.gov.hmcts.reform.civil.model.docmosis.lip.LipFormParty;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE;
import static uk.gov.hmcts.reform.civil.utils.DateUtils.getRequiredDateBeforeFourPm;

@Component
@RequiredArgsConstructor
public class ClaimFormMapper {

    private static final String STANDARD_INTEREST_RATE = "8";
    public static final String EXPLANATION_OF_INTEREST_RATE = "The claimant reserves the right to claim interest under "
        + "Section 69 of the County Courts Act 1984";
    public static final String INTEREST_START_FROM_CLAIM_ISSUED_DATE = "From the date the claim was issued";
    private final InterestCalculator interestCalculator;

    public ClaimForm toClaimForm(CaseData caseData) {
        BigDecimal interest = interestCalculator.calculateInterest(caseData);
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
        String totalClaimAmount = Optional.ofNullable(caseData.getTotalClaimAmount())
            .map(amount -> amount.setScale(2))
            .map(BigDecimal::toString)
            .orElse("0");
        return ClaimForm.builder()
            .totalInterestAmount(interest != null ? interest.toString() : null)
            .howTheInterestWasCalculated(Optional.ofNullable(caseData.getInterestClaimOptions()).map(
                InterestClaimOptions::getDescription).orElse(null))
            .interestRate(getInterestRate(caseData))
            .interestExplanationText(interest != null ? generateInterestRateExplanation(caseData) : null)
            .interestFromDate(interest != null && interest.compareTo(BigDecimal.ZERO) > 0 ? getInterestFromDate(caseData) : null)
            .interestEndDate(interest != null && interest.compareTo(BigDecimal.ZERO) > 0 ? getInterestEndDate(caseData) : null)
            .interestEndDateDescription(interest != null ? caseData.getBreakDownInterestDescription() : null)
            .whenAreYouClaimingInterestFrom(interest != null ? generateWhenAreYouPlanningInterestFrom(
                caseData) : null)
            .timelineEvents(getTimeLine(caseData.getTimelineOfEvents()))
            .totalClaimAmount(totalClaimAmount)
            .interestAmount(interest != null ? interest.toString() : null)
            .claimAmount(caseData.getClaimAmountBreakupDetails())
            .claimFee(getClaimFee(caseData).toString())
            .totalAmountOfClaim(calculateTotalAmountOfClaim(caseData, interest))
            .descriptionOfClaim(caseData.getDetailsOfClaim())
            .claimant(claimant)
            .defendant(LipFormParty.toLipFormParty(
                caseData.getRespondent1(),
                getCorrespondenceAddress(defendantDetails),
                getContactPerson(defendantDetails)
            ))
            .generationDate(LocalDateTime.now())
            .claimIssuedDate(caseData.getIssueDate())
            .claimNumber(caseData.getLegacyCaseReference())
            .flightDelayDetails(getFlightDelayDetails(caseData))
            .evidenceList(Optional.ofNullable(caseData.getSpeclistYourEvidenceList())
                              .map(Collection::stream)
                              .map(evidenceStream -> evidenceStream
                                  .map(EvidenceTemplateData::toEvidenceTemplateData)
                                  .toList())
                              .orElse(Collections.emptyList()))
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
            return (Optional.ofNullable(caseData.getTotalClaimAmount()).orElse(BigDecimal.ZERO)
                .add(interest)
                .add(getClaimFee(caseData))).setScale(2).toString();
        }
        return (Optional.ofNullable(caseData.getTotalClaimAmount()).orElse(BigDecimal.ZERO)
            .add(getClaimFee(caseData))).setScale(2).toString();
    }

    private BigDecimal getClaimFee(CaseData caseData) {
        BigDecimal claimFee = MonetaryConversions.penniesToPounds(caseData.getCalculatedClaimFeeInPence());
        if (caseData.isHelpWithFees()
            && caseData.getOutstandingFeeInPounds() != null) {
            claimFee = caseData.getOutstandingFeeInPounds();
        }
        return claimFee;
    }

    @JsonIgnore
    public List<Timeline> getTimeLine(List<TimelineOfEvents> timelineOfEvents) {
        return Optional.ofNullable(timelineOfEvents)
            .map(Collection::stream)
            .map(timelineOfEventsStream -> timelineOfEventsStream
                .map(item -> new Timeline(
                    item.getValue().getTimelineDate(),
                    item.getValue().getTimelineDescription()
                ))
                .toList())
            .orElse(Collections.emptyList());

    }

    @JsonIgnore
    private FlightDelayDetails getFlightDelayDetails(CaseData caseData) {
        return Objects.nonNull(caseData.getFlightDelayDetails()) && caseData.getRespondent1().isCompany()
            ? FlightDelayDetails.builder().flightNumber(caseData.getFlightDelayDetails().getFlightNumber())
            .nameOfAirline(caseData.getFlightDelayDetails().getNameOfAirline()).scheduledDate(caseData.getFlightDelayDetails().getScheduledDate()).build()
            : null;
    }
}
