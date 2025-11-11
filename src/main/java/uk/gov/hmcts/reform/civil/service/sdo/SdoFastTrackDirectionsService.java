package uk.gov.hmcts.reform.civil.service.sdo;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Service
public class SdoFastTrackDirectionsService {

    public boolean hasFastAdditionalDirections(CaseData caseData, FastTrack direction) {
        List<FastTrack> selections = caseData.getTrialAdditionalDirectionsForFastTrack() != null
            ? caseData.getTrialAdditionalDirectionsForFastTrack()
            : caseData.getFastClaims();

        return selections != null
            && direction != null
            && selections.contains(direction);
    }

    public boolean hasFastTrackVariable(CaseData caseData, FastTrackVariable variable) {
        switch (variable) {
            case ALT_DISPUTE_RESOLUTION:
                return caseData.getFastTrackAltDisputeResolutionToggle() != null;
            case VARIATION_OF_DIRECTIONS:
                return caseData.getFastTrackVariationOfDirectionsToggle() != null;
            case SETTLEMENT:
                return caseData.getFastTrackSettlementToggle() != null;
            case DISCLOSURE_OF_DOCUMENTS:
                return caseData.getFastTrackDisclosureOfDocumentsToggle() != null;
            case WITNESS_OF_FACT:
                return caseData.getFastTrackWitnessOfFactToggle() != null;
            case SCHEDULES_OF_LOSS:
                return caseData.getFastTrackSchedulesOfLossToggle() != null;
            case COSTS:
                return caseData.getFastTrackCostsToggle() != null;
            case TRIAL:
                return caseData.getFastTrackTrialToggle() != null;
            case METHOD_TOGGLE:
                return true;
            case ADD_NEW_DIRECTIONS:
                return caseData.getFastTrackAddNewDirections() != null;
            case TRIAL_DATE_TO_TOGGLE:
                return caseData.getFastTrackHearingTime() != null
                    && caseData.getFastTrackHearingTime().getDateToToggle() != null;
            case WELSH_TOGGLE:
                return caseData.getSdoR2FastTrackUseOfWelshToggle() != null;
            case TRIAL_BUNDLE_TOGGLE:
                return caseData.getFastTrackTrialBundleToggle() != null;
            default:
                throw new IllegalStateException("Unhandled fast-track variable: " + variable);
        }
    }

}
