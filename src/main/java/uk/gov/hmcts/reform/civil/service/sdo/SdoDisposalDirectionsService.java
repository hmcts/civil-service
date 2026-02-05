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
        return switch (variableName) {
            case "disposalHearingBundleToggle" -> caseData.getDisposalHearingBundleToggle() != null;
            case "disposalHearingClaimSettlingToggle" -> caseData.getDisposalHearingClaimSettlingToggle() != null;
            case "disposalHearingCostsToggle" -> caseData.getDisposalHearingCostsToggle() != null;
            case "disposalHearingAddNewDirections" -> caseData.getDisposalHearingAddNewDirections() != null;
            case "disposalHearingDisclosureOfDocumentsToggle" -> caseData.getDisposalHearingDisclosureOfDocumentsToggle() != null;
            case "disposalHearingWitnessOfFactToggle" -> caseData.getDisposalHearingWitnessOfFactToggle() != null;
            case "disposalHearingMedicalEvidenceToggle" -> caseData.getDisposalHearingMedicalEvidenceToggle() != null;
            case "disposalHearingQuestionsToExpertsToggle" -> caseData.getDisposalHearingQuestionsToExpertsToggle() != null;
            case "disposalHearingSchedulesOfLossToggle" -> caseData.getDisposalHearingSchedulesOfLossToggle() != null;
            case "disposalHearingFinalDisposalHearingToggle" -> caseData.getDisposalHearingFinalDisposalHearingToggle() != null;
            case "disposalHearingMethodToggle" -> true;
            case "disposalHearingDateToToggle" -> caseData.getTrialHearingTimeDJ() != null
                    && caseData.getTrialHearingTimeDJ().getDateToToggle() != null;
            default -> false;
        };
    }

    public String getHearingTimeLabel(CaseData caseData) {
        DisposalHearingHearingTime hearingTime = caseData.getDisposalHearingHearingTime();

        if (!hasTimeSelection(hearingTime)) {
            return "";
        }

        if (isOtherSelection(hearingTime)) {
            return buildOtherLength(hearingTime);
        }

        return hearingTime.getTime().getLabel().toLowerCase(Locale.ROOT);
    }

    private boolean hasTimeSelection(DisposalHearingHearingTime hearingTime) {
        return Optional.ofNullable(hearingTime)
            .map(DisposalHearingHearingTime::getTime)
            .map(DisposalHearingFinalDisposalHearingTimeEstimate::getLabel)
            .isPresent();
    }

    private boolean isOtherSelection(DisposalHearingHearingTime hearingTime) {
        return Optional.ofNullable(hearingTime)
            .map(DisposalHearingHearingTime::getTime)
            .map(DisposalHearingFinalDisposalHearingTimeEstimate::getLabel)
            .filter(OTHER::equals)
            .isPresent();
    }

    private String buildOtherLength(DisposalHearingHearingTime hearingTime) {
        int hours = parseToInt(hearingTime.getOtherHours());
        int minutes = parseToInt(hearingTime.getOtherMinutes());
        StringBuilder otherLength = new StringBuilder();

        if (hours > 0) {
            otherLength.append(hours).append(hours == 1 ? " hour" : " hours");
        }

        if (minutes > 0) {
            if (!otherLength.isEmpty()) {
                otherLength.append(" ");
            }
            otherLength.append(minutes).append(minutes == 1 ? " minute" : MINUTES);
        }

        return otherLength.toString();
    }

    private int parseToInt(String value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
