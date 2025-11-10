package uk.gov.hmcts.reform.civil.service.sdo;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingBundleType;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingBundle;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearing;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class SdoDisposalDirectionsService {

    private static final String OTHER = "Other";
    private static final String MINUTES = " minutes";

    public String getFinalHearingTimeLabel(CaseData caseData) {
        DisposalHearingFinalDisposalHearing finalHearing = caseData.getDisposalHearingFinalDisposalHearing();

        if (finalHearing != null && finalHearing.getTime() != null) {
            return finalHearing.getTime().getLabel();
        }

        return "";
    }

    public String getTelephoneHearingLabel(CaseData caseData) {
        DisposalHearingMethodTelephoneHearing hearing = caseData.getDisposalHearingMethodTelephoneHearing();

        if (hearing != null) {
            return hearing.getLabel();
        }

        return "";
    }

    public String getVideoConferenceHearingLabel(CaseData caseData) {
        DisposalHearingMethodVideoConferenceHearing hearing = caseData.getDisposalHearingMethodVideoConferenceHearing();

        if (hearing != null) {
            return hearing.getLabel();
        }

        return "";
    }

    public String getBundleTypeText(CaseData caseData) {
        DisposalHearingBundle bundle = caseData.getDisposalHearingBundle();

        if (bundle != null) {
            List<DisposalHearingBundleType> types = bundle.getType();
            StringBuilder labels = new StringBuilder();

            if (types != null && !types.isEmpty()) {
                if (types.size() == 3) {
                    labels.append(DisposalHearingBundleType.DOCUMENTS.getLabel());
                    labels.append(" / ").append(DisposalHearingBundleType.ELECTRONIC.getLabel());
                    labels.append(" / ").append(DisposalHearingBundleType.SUMMARY.getLabel());
                } else if (types.size() == 2) {
                    labels.append(types.get(0).getLabel());
                    labels.append(" / ").append(types.get(1).getLabel());
                } else {
                    labels.append(types.get(0).getLabel());
                }
                return labels.toString();
            }

        }

        return "";
    }

    public boolean hasDisposalVariable(CaseData caseData, String variableName) {
        switch (variableName) {
            case "disposalHearingBundleToggle":
                return caseData.getDisposalHearingBundleToggle() != null;
            case "disposalHearingClaimSettlingToggle":
                return caseData.getDisposalHearingClaimSettlingToggle() != null;
            case "disposalHearingCostsToggle":
                return caseData.getDisposalHearingCostsToggle() != null;
            case "disposalHearingAddNewDirections":
                return caseData.getDisposalHearingAddNewDirections() != null;
            case "disposalHearingDisclosureOfDocumentsToggle":
                return caseData.getDisposalHearingDisclosureOfDocumentsToggle() != null;
            case "disposalHearingWitnessOfFactToggle":
                return caseData.getDisposalHearingWitnessOfFactToggle() != null;
            case "disposalHearingMedicalEvidenceToggle":
                return caseData.getDisposalHearingMedicalEvidenceToggle() != null;
            case "disposalHearingQuestionsToExpertsToggle":
                return caseData.getDisposalHearingQuestionsToExpertsToggle() != null;
            case "disposalHearingSchedulesOfLossToggle":
                return caseData.getDisposalHearingSchedulesOfLossToggle() != null;
            case "disposalHearingFinalDisposalHearingToggle":
                return caseData.getDisposalHearingFinalDisposalHearingToggle() != null;
            case "disposalHearingMethodToggle":
                return true;
            case "disposalHearingDateToToggle":
                return caseData.getTrialHearingTimeDJ() != null
                    && caseData.getTrialHearingTimeDJ().getDateToToggle() != null;
            default:
                return false;
        }
    }

    public String getHearingTimeLabel(CaseData caseData) {
        DisposalHearingHearingTime hearingTime = caseData.getDisposalHearingHearingTime();

        String label = "";

        if (Optional.ofNullable(hearingTime)
            .map(DisposalHearingHearingTime::getTime)
            .map(DisposalHearingFinalDisposalHearingTimeEstimate::getLabel).isPresent()) {
            if (hearingTime.getTime().getLabel().equals(OTHER)) {
                StringBuilder otherLength = new StringBuilder();
                if (hearingTime.getOtherHours() != null
                    && Integer.parseInt(hearingTime.getOtherHours()) != 0) {
                    String hourString = Integer.parseInt(hearingTime.getOtherHours()) == 1
                        ? " hour" : " hours";
                    otherLength.append(hearingTime.getOtherHours().trim()).append(hourString);
                }
                if (hearingTime.getOtherMinutes() != null
                    && Integer.parseInt(hearingTime.getOtherMinutes()) != 0) {
                    String minuteString = Integer.parseInt(hearingTime.getOtherMinutes()) == 1
                        ? " minute" : MINUTES;
                    String spaceBeforeMinute = otherLength.toString().contains("hour") ? " " : "";
                    otherLength.append(spaceBeforeMinute)
                        .append(hearingTime.getOtherMinutes().trim())
                        .append(minuteString);
                }
                return otherLength.toString();
            }
            label = hearingTime.getTime().getLabel().toLowerCase(Locale.ROOT);
        }

        return label;
    }
}
