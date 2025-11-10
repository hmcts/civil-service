package uk.gov.hmcts.reform.civil.service.sdo;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackTrialBundleType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackTrial;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
public class SdoFastTrackDirectionsService {

    private static final String MINUTES = " minutes";
    private static final String OTHER = "Other";
    private static final String FAST_TRACK_ALLOCATION_BASE = "The claim is allocated to the Fast Track";
    private static final String FAST_TRACK_ALLOCATION_WITH_COMPLEXITY = " and is assigned to complexity %s";
    private static final String FAST_TRACK_ALLOCATION_NO_COMPLEXITY = " and is not assigned to a complexity band";
    private static final String FAST_TRACK_ALLOCATION_REASON = " because %s";

    public boolean hasFastAdditionalDirections(CaseData caseData, String additionalDirection) {
        FastTrack direction = mapAdditionalDirection(additionalDirection);
        List<FastTrack> selections = caseData.getTrialAdditionalDirectionsForFastTrack() != null
            ? caseData.getTrialAdditionalDirectionsForFastTrack()
            : caseData.getFastClaims();

        return selections != null
            && direction != null
            && selections.contains(direction);
    }

    public boolean hasFastTrackVariable(CaseData caseData, String variableName) {
        switch (variableName) {
            case "fastTrackAltDisputeResolutionToggle":
                return caseData.getFastTrackAltDisputeResolutionToggle() != null;
            case "fastTrackVariationOfDirectionsToggle":
                return caseData.getFastTrackVariationOfDirectionsToggle() != null;
            case "fastTrackSettlementToggle":
                return caseData.getFastTrackSettlementToggle() != null;
            case "fastTrackDisclosureOfDocumentsToggle":
                return caseData.getFastTrackDisclosureOfDocumentsToggle() != null;
            case "fastTrackWitnessOfFactToggle":
                return caseData.getFastTrackWitnessOfFactToggle() != null;
            case "fastTrackSchedulesOfLossToggle":
                return caseData.getFastTrackSchedulesOfLossToggle() != null;
            case "fastTrackCostsToggle":
                return caseData.getFastTrackCostsToggle() != null;
            case "fastTrackTrialToggle":
                return caseData.getFastTrackTrialToggle() != null;
            case "fastTrackMethodToggle":
                return true;
            case "fastTrackAddNewDirections":
                return caseData.getFastTrackAddNewDirections() != null;
            case "fastTrackTrialDateToToggle":
                return caseData.getFastTrackHearingTime() != null
                    && caseData.getFastTrackHearingTime().getDateToToggle() != null;
            case "sdoR2FastTrackUseOfWelshToggle":
                return caseData.getSdoR2FastTrackUseOfWelshToggle() != null;
            case "fastTrackTrialBundleToggle":
                return caseData.getFastTrackTrialBundleToggle() != null;
            default:
                return false;
        }
    }

    public String getFastTrackMethodTelephoneHearingLabel(CaseData caseData) {
        FastTrackMethodTelephoneHearing hearing = caseData.getFastTrackMethodTelephoneHearing();

        if (hearing != null) {
            return hearing.getLabel();
        }

        return "";
    }

    public String getFastTrackMethodVideoConferenceHearingLabel(CaseData caseData) {
        FastTrackMethodVideoConferenceHearing hearing = caseData.getFastTrackMethodVideoConferenceHearing();

        if (hearing != null) {
            return hearing.getLabel();
        }

        return "";
    }

    public String getFastTrackTrialBundleTypeText(CaseData caseData) {
        FastTrackTrial trial = caseData.getFastTrackTrial();

        if (trial != null && trial.getType() != null && !trial.getType().isEmpty()) {
            List<FastTrackTrialBundleType> types = trial.getType();
            StringBuilder labels = new StringBuilder();

            if (types.size() == 3) {
                labels.append(FastTrackTrialBundleType.DOCUMENTS.getLabel());
                labels.append(" / ").append(FastTrackTrialBundleType.ELECTRONIC.getLabel());
                labels.append(" / ").append(FastTrackTrialBundleType.SUMMARY.getLabel());
            } else if (types.size() == 2) {
                labels.append(types.get(0).getLabel());
                labels.append(" / ").append(types.get(1).getLabel());
            } else {
                labels.append(types.get(0).getLabel());
            }

            return labels.toString();
        }

        return "";
    }

    public String getFastClaimsHearingTimeLabel(CaseData caseData) {
        FastTrackHearingTime hearingTime = caseData.getFastTrackHearingTime();

        String hearingLabel = "";

        if (Optional.ofNullable(hearingTime)
            .map(FastTrackHearingTime::getHearingDuration)
            .map(FastTrackHearingTimeEstimate::getLabel).isPresent()) {
            if (OTHER.equals(hearingTime.getHearingDuration().getLabel())) {
                StringBuilder otherLength = new StringBuilder();
                if (hearingTime.getOtherHours() != null) {
                    otherLength.append(hearingTime.getOtherHours().trim()).append(" hours ");
                }
                if (hearingTime.getOtherMinutes() != null) {
                    otherLength.append(hearingTime.getOtherMinutes().trim()).append(MINUTES);
                }
                return otherLength.toString();
            }

            hearingLabel = hearingTime.getHearingDuration().getLabel();
        }

        return hearingLabel;
    }

    public String getFastTrackAllocation(CaseData caseData) {
        FastTrackAllocation fastTrackAllocation = caseData.getFastTrackAllocation();
        if (fastTrackAllocation == null) {
            return "";
        }

        String reasons = buildFastTrackAllocationReason(fastTrackAllocation);
        YesOrNo assignComplexityBand = fastTrackAllocation.getAssignComplexityBand();

        if (assignComplexityBand == NO) {
            return FAST_TRACK_ALLOCATION_BASE + FAST_TRACK_ALLOCATION_NO_COMPLEXITY + reasons;
        } else if (assignComplexityBand == YES && fastTrackAllocation.getBand() != null) {
            String band = String.format(
                FAST_TRACK_ALLOCATION_WITH_COMPLEXITY,
                fastTrackAllocation.getBand().getLabel().toLowerCase()
            );
            return FAST_TRACK_ALLOCATION_BASE + band + reasons;
        }

        return "";
    }

    private String buildFastTrackAllocationReason(FastTrackAllocation fastTrackAllocation) {
        if (fastTrackAllocation.getReasons() != null && !fastTrackAllocation.getReasons().isBlank()) {
            return String.format(FAST_TRACK_ALLOCATION_REASON, fastTrackAllocation.getReasons());
        }
        return "";
    }

    private FastTrack mapAdditionalDirection(String additionalDirection) {
        switch (additionalDirection) {
            case "fastClaimBuildingDispute":
                return FastTrack.fastClaimBuildingDispute;
            case "fastClaimClinicalNegligence":
                return FastTrack.fastClaimClinicalNegligence;
            case "fastClaimCreditHire":
                return FastTrack.fastClaimCreditHire;
            case "fastClaimEmployersLiability":
                return FastTrack.fastClaimEmployersLiability;
            case "fastClaimHousingDisrepair":
                return FastTrack.fastClaimHousingDisrepair;
            case "fastClaimPersonalInjury":
                return FastTrack.fastClaimPersonalInjury;
            case "fastClaimRoadTrafficAccident":
                return FastTrack.fastClaimRoadTrafficAccident;
            default:
                return null;
        }
    }
}
