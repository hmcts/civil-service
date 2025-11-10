package uk.gov.hmcts.reform.civil.service.sdo;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SmallClaimsMediation;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsWitnessStatement;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsMediation;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class SdoSmallClaimsDirectionsService {

    private static final String MINUTES = " minutes";
    private static final String OTHER = "Other";

    public boolean hasSmallAdditionalDirections(CaseData caseData, String additionalDirection) {
        SmallTrack direction = mapAdditionalDirection(additionalDirection);
        List<SmallTrack> selections = caseData.getDrawDirectionsOrderSmallClaimsAdditionalDirections() != null
            ? caseData.getDrawDirectionsOrderSmallClaimsAdditionalDirections()
            : caseData.getSmallClaims();

        return selections != null
            && direction != null
            && selections.contains(direction);
    }

    public String getSmallClaimsHearingTimeLabel(CaseData caseData) {
        SmallClaimsHearing hearing = caseData.getSmallClaimsHearing();
        String label = "";

        if (Optional.ofNullable(hearing)
            .map(SmallClaimsHearing::getTime)
            .map(SmallClaimsTimeEstimate::getLabel).isPresent()) {
            if (OTHER.equals(hearing.getTime().getLabel())) {
                StringBuilder otherLength = new StringBuilder();
                if (hearing.getOtherHours() != null) {
                    otherLength.append(hearing.getOtherHours().toString().trim()).append(" hours ");
                }
                if (hearing.getOtherMinutes() != null) {
                    otherLength.append(hearing.getOtherMinutes().toString().trim()).append(MINUTES);
                }
                return otherLength.toString();
            }
            label = hearing.getTime().getLabel().toLowerCase(Locale.ROOT);
        }

        return label;
    }

    public String getSmallClaimsMethodTelephoneHearingLabel(CaseData caseData) {
        SmallClaimsMethodTelephoneHearing value = caseData.getSmallClaimsMethodTelephoneHearing();
        if (value != null) {
            return value.getLabel();
        }
        return "";
    }

    public String getSmallClaimsMethodVideoConferenceHearingLabel(CaseData caseData) {
        SmallClaimsMethodVideoConferenceHearing value = caseData.getSmallClaimsMethodVideoConferenceHearing();
        if (value != null) {
            return value.getLabel();
        }
        return "";
    }

    public boolean hasSmallClaimsVariable(CaseData caseData, String variableName) {
        switch (variableName) {
            case "smallClaimsHearingToggle":
                return caseData.getSmallClaimsHearingToggle() != null;
            case "smallClaimsMethodToggle":
                return true;
            case "smallClaimsDocumentsToggle":
                return caseData.getSmallClaimsDocumentsToggle() != null;
            case "smallClaimsWitnessStatementToggle":
                return caseData.getSmallClaimsWitnessStatementToggle() != null;
            case "smallClaimsFlightDelayToggle":
                return caseData.getSmallClaimsFlightDelayToggle() != null;
            case "smallClaimsNumberOfWitnessesToggle":
                SmallClaimsWitnessStatement witnessStatement = caseData.getSmallClaimsWitnessStatement();
                return witnessStatement != null && witnessStatement.getSmallClaimsNumberOfWitnessesToggle() != null;
            case "smallClaimsAddNewDirections":
                return caseData.getSmallClaimsAddNewDirections() != null;
            case "smallClaimsMediationSectionToggle":
                return caseData.getSmallClaimsMediationSectionStatement() != null;
            case "sdoR2SmallClaimsUseOfWelshToggle":
                return caseData.getSdoR2SmallClaimsUseOfWelshToggle() != null;
            default:
                return false;
        }
    }

    public boolean showCarmMediationSection(CaseData caseData, boolean carmEnabled) {
        return caseData.getSmallClaimsMediationSectionStatement() != null
            && getSmallClaimsMediationText(caseData) != null
            && carmEnabled;
    }

    public String getSmallClaimsMediationText(CaseData caseData) {
        SmallClaimsMediation mediation = caseData.getSmallClaimsMediationSectionStatement();
        if (mediation != null) {
            return mediation.getInput();
        }
        return null;
    }

    public boolean showCarmMediationSectionDrh(CaseData caseData, boolean carmEnabled) {
        return caseData.getSdoR2SmallClaimsMediationSectionStatement() != null
            && getSmallClaimsMediationTextDrh(caseData) != null
            && carmEnabled;
    }

    public String getSmallClaimsMediationTextDrh(CaseData caseData) {
        SdoR2SmallClaimsMediation mediation = caseData.getSdoR2SmallClaimsMediationSectionStatement();
        if (mediation != null) {
            return mediation.getInput();
        }
        return null;
    }

    private SmallTrack mapAdditionalDirection(String additionalDirection) {
        switch (additionalDirection) {
            case "smallClaimCreditHire":
                return SmallTrack.smallClaimCreditHire;
            case "smallClaimRoadTrafficAccident":
                return SmallTrack.smallClaimRoadTrafficAccident;
            case "smallClaimDisputeResolutionHearing":
                return SmallTrack.smallClaimDisputeResolutionHearing;
            case "smallClaimFlightDelay":
                return SmallTrack.smallClaimFlightDelay;
            default:
                return null;
        }
    }
}
