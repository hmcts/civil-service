package uk.gov.hmcts.reform.civil.helpers.sdo;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingBundleType;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackTrialBundleType;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingBundle;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearing;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackTrial;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsTimeEstimate;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class SdoHelper {

    private SdoHelper() {
        // Utility class, no instances
    }

    private static final String FAST_TRACK_ALLOCATION_BASE = "The claim is allocated to the Fast Track";
    private static final String FAST_TRACK_ALLOCATION_WTIH_COMPLEXITY = " and is assigned to complexity %s";
    private static final String FAST_TRACK_ALLOCATION_NO_COMPLEXITY = " and is not assigned to a complexity band";
    private static final String FAST_TRACK_ALLOCATION_REASON = " because %s";

    public static boolean isSmallClaimsTrack(CaseData caseData) {
        YesOrNo drawDirectionsOrderRequired = caseData.getDrawDirectionsOrderRequired();
        YesOrNo drawDirectionsOrderSmallClaims = caseData.getDrawDirectionsOrderSmallClaims();
        ClaimsTrack claimsTrack = caseData.getClaimsTrack();

        Boolean smallClaimsPath1 = (drawDirectionsOrderRequired == NO)
            && (claimsTrack == ClaimsTrack.smallClaimsTrack);
        Boolean smallClaimsPath2 = (drawDirectionsOrderRequired == YES)
            && (drawDirectionsOrderSmallClaims == YES);

        return smallClaimsPath1 || smallClaimsPath2;
    }

    public static boolean isFastTrack(CaseData caseData) {
        YesOrNo drawDirectionsOrderRequired = caseData.getDrawDirectionsOrderRequired();
        YesOrNo drawDirectionsOrderSmallClaims = caseData.getDrawDirectionsOrderSmallClaims();
        ClaimsTrack claimsTrack = caseData.getClaimsTrack();
        OrderType orderType = caseData.getOrderType();

        Boolean fastTrackPath1 = (drawDirectionsOrderRequired == NO)
            && (claimsTrack == ClaimsTrack.fastTrack);
        Boolean fastTrackPath2 = (drawDirectionsOrderRequired == YES)
            && (drawDirectionsOrderSmallClaims == NO) && (orderType == OrderType.DECIDE_DAMAGES);

        return fastTrackPath1 || fastTrackPath2;
    }

    public static boolean hasSharedVariable(CaseData caseData, String variableName) {
        switch (variableName) {

            case "applicant2":
                return caseData.getApplicant2() != null;
            case "respondent2":
                return caseData.getRespondent2() != null;
            default:
                return false;
        }
    }

    public static SmallTrack getSmallClaimsAdditionalDirectionEnum(String additionalDirection) {
        switch (additionalDirection) {
            case "smallClaimCreditHire":
                return SmallTrack.smallClaimCreditHire;
            case "smallClaimRoadTrafficAccident":
                return SmallTrack.smallClaimRoadTrafficAccident;
            default:
                return null;
        }
    }

    public static boolean hasSmallAdditionalDirections(CaseData caseData, String additionalDirection) {
        SmallTrack additionalDirectionEnum = getSmallClaimsAdditionalDirectionEnum(additionalDirection);
        List<SmallTrack> smallClaims = caseData.getDrawDirectionsOrderSmallClaimsAdditionalDirections() != null
            ? caseData.getDrawDirectionsOrderSmallClaimsAdditionalDirections() : caseData.getSmallClaims();
        boolean hasDirection;

        if ((smallClaims != null) && (additionalDirectionEnum != null)) {
            hasDirection = smallClaims.contains(additionalDirectionEnum);
        } else {
            hasDirection = false;
        }

        return hasDirection;
    }

    public static String getDisposalHearingTimeLabel(CaseData caseData) {
        DisposalHearingHearingTime disposalHearingHearingTime = caseData.getDisposalHearingHearingTime();

        String hearingTimeEstimateLabel = "";

        if (Optional.ofNullable(caseData.getDisposalHearingHearingTime())
            .map(DisposalHearingHearingTime::getTime)
            .map(DisposalHearingFinalDisposalHearingTimeEstimate::getLabel).isPresent()) {
            if (disposalHearingHearingTime.getTime().getLabel().equals("Other")) {
                StringBuilder otherLength = new StringBuilder();
                if (disposalHearingHearingTime.getOtherHours() != null
                    && Integer.parseInt(disposalHearingHearingTime.getOtherHours()) != 0) {
                    String hourString = Integer.parseInt(disposalHearingHearingTime.getOtherHours()) == 1
                        ? " hour" : " hours";
                    otherLength.append(disposalHearingHearingTime.getOtherHours().trim() + hourString);
                }
                if (disposalHearingHearingTime.getOtherMinutes() != null
                    && Integer.parseInt(disposalHearingHearingTime.getOtherMinutes()) != 0) {
                    String minuteString = Integer.parseInt(disposalHearingHearingTime.getOtherMinutes()) == 1
                        ? " minute" : " minutes";
                    String spaceBeforeMinute = otherLength.toString().contains("hour") ? " " : "";
                    otherLength.append(spaceBeforeMinute
                                           + disposalHearingHearingTime.getOtherMinutes().trim()
                                           + minuteString);
                }
                return otherLength.toString();
            }

            hearingTimeEstimateLabel = disposalHearingHearingTime.getTime().getLabel().toLowerCase(Locale.ROOT);
        }

        return hearingTimeEstimateLabel;
    }

    public static String getSmallClaimsHearingTimeLabel(CaseData caseData) {
        SmallClaimsHearing smallClaimHearing = caseData.getSmallClaimsHearing();

        String hearingTimeEstimateLabel = "";

        if (Optional.ofNullable(caseData.getSmallClaimsHearing())
            .map(SmallClaimsHearing::getTime)
            .map(SmallClaimsTimeEstimate::getLabel).isPresent()) {
            if (smallClaimHearing.getTime().getLabel().equals("Other")) {
                StringBuilder otherLength = new StringBuilder();
                if (smallClaimHearing.getOtherHours() != null) {
                    otherLength.append(smallClaimHearing.getOtherHours().toString().trim() +
                                           " hours ");
                }
                if (smallClaimHearing.getOtherMinutes() != null) {
                    otherLength.append(smallClaimHearing.getOtherMinutes().toString().trim() + " minutes");
                }
                return otherLength.toString();
            }

            hearingTimeEstimateLabel = smallClaimHearing.getTime().getLabel().toLowerCase(Locale.ROOT);
        }

        return hearingTimeEstimateLabel;
    }

    public static String getFastClaimsHearingTimeLabel(CaseData caseData) {
        FastTrackHearingTime fastTrackHearingTime = caseData.getFastTrackHearingTime();

        String fastTrackHearingTimeLabel = "";

        if (Optional.ofNullable(caseData.getFastTrackHearingTime())
            .map(FastTrackHearingTime::getHearingDuration)
            .map(FastTrackHearingTimeEstimate::getLabel).isPresent()) {
            if (fastTrackHearingTime.getHearingDuration().getLabel().equals("Other")) {
                StringBuilder otherLength = new StringBuilder();
                if (fastTrackHearingTime.getOtherHours() != null) {
                    otherLength.append(fastTrackHearingTime.getOtherHours().toString().trim() +
                                           " hours ");
                }
                if (fastTrackHearingTime.getOtherMinutes() != null) {
                    otherLength.append(fastTrackHearingTime.getOtherMinutes().toString().trim() + " minutes");
                }
                return otherLength.toString();
            }

            fastTrackHearingTimeLabel = fastTrackHearingTime.getHearingDuration().getLabel();
        }

        return fastTrackHearingTimeLabel;
    }

    public static String getSmallClaimsMethodTelephoneHearingLabel(CaseData caseData) {
        SmallClaimsMethodTelephoneHearing smallClaimsMethodTelephoneHearing =
            caseData.getSmallClaimsMethodTelephoneHearing();

        if (smallClaimsMethodTelephoneHearing != null) {
            return smallClaimsMethodTelephoneHearing.getLabel();
        }

        return "";
    }

    public static String getSmallClaimsMethodVideoConferenceHearingLabel(CaseData caseData) {
        SmallClaimsMethodVideoConferenceHearing smallClaimsMethodVideoConferenceHearing =
            caseData.getSmallClaimsMethodVideoConferenceHearing();

        if (smallClaimsMethodVideoConferenceHearing != null) {
            return smallClaimsMethodVideoConferenceHearing.getLabel();
        }

        return "";
    }

    public static boolean hasSmallClaimsVariable(CaseData caseData, String variableName) {
        switch (variableName) {
            case "smallClaimsHearingToggle":
                return caseData.getSmallClaimsHearingToggle() != null;
            case "smallClaimsMethodToggle":
                return caseData.getSmallClaimsMethodToggle() != null;
            case "smallClaimsDocumentsToggle":
                return caseData.getSmallClaimsDocumentsToggle() != null;
            case "smallClaimsWitnessStatementToggle":
                return caseData.getSmallClaimsWitnessStatementToggle() != null;
            case "smallClaimsNumberOfWitnessesToggle":
                return caseData.getSmallClaimsWitnessStatement() != null
                    && caseData.getSmallClaimsWitnessStatement().getSmallClaimsNumberOfWitnessesToggle() != null;
            case "smallClaimsAddNewDirections":
                return caseData.getSmallClaimsAddNewDirections() != null;
            default:
                return false;
        }
    }

    public static FastTrack getFastTrackAdditionalDirectionEnum(String additionalDirection) {
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

    public static boolean hasFastAdditionalDirections(CaseData caseData, String additionalDirection) {
        FastTrack additionalDirectionEnum = getFastTrackAdditionalDirectionEnum(additionalDirection);
        List<FastTrack> fastClaims = caseData.getTrialAdditionalDirectionsForFastTrack() != null
            ? caseData.getTrialAdditionalDirectionsForFastTrack() : caseData.getFastClaims();
        boolean hasDirection;

        if ((fastClaims != null) && (additionalDirectionEnum != null)) {
            hasDirection = fastClaims.contains(additionalDirectionEnum);
        } else {
            hasDirection = false;
        }

        return hasDirection;
    }

    public static boolean hasFastTrackVariable(CaseData caseData, String variableName) {
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
                return caseData.getFastTrackMethodToggle() != null;
            case "fastTrackAddNewDirections":
                return caseData.getFastTrackAddNewDirections() != null;
            case "fastTrackTrialDateToToggle":
                return caseData.getFastTrackHearingTime() != null
                    && caseData.getFastTrackHearingTime().getDateToToggle() != null;
            default:
                return false;
        }
    }

    public static String getFastTrackMethodTelephoneHearingLabel(CaseData caseData) {
        FastTrackMethodTelephoneHearing fastTrackMethodTelephoneHearing =
            caseData.getFastTrackMethodTelephoneHearing();

        if (fastTrackMethodTelephoneHearing != null) {
            return fastTrackMethodTelephoneHearing.getLabel();
        }

        return "";
    }

    public static String getFastTrackMethodVideoConferenceHearingLabel(CaseData caseData) {
        FastTrackMethodVideoConferenceHearing fastTrackMethodVideoConferenceHearing =
            caseData.getFastTrackMethodVideoConferenceHearing();

        if (fastTrackMethodVideoConferenceHearing != null) {
            return fastTrackMethodVideoConferenceHearing.getLabel();
        }

        return "";
    }

    public static String getFastTrackTrialBundleTypeText(CaseData caseData) {
        FastTrackTrial fastTrackTrial = caseData.getFastTrackTrial();

        if (fastTrackTrial != null) {
            List<FastTrackTrialBundleType> types = fastTrackTrial.getType();
            StringBuilder stringBuilder = new StringBuilder();

            if (fastTrackTrial.getType().size() == 3) {
                stringBuilder.append(FastTrackTrialBundleType.DOCUMENTS.getLabel());
                stringBuilder.append(" / " + FastTrackTrialBundleType.ELECTRONIC.getLabel());
                stringBuilder.append(" / " + FastTrackTrialBundleType.SUMMARY.getLabel());
            } else if (fastTrackTrial.getType().size() == 2) {
                stringBuilder.append(types.get(0).getLabel());
                stringBuilder.append(" / " + types.get(1).getLabel());
            } else {
                stringBuilder.append(types.get(0).getLabel());
            }

            return stringBuilder.toString();
        }

        return "";
    }

    public static String getFastTrackAllocation(CaseData caseData, boolean fastTrackUpliftsEnabled) {
        if (fastTrackUpliftsEnabled) {
            FastTrackAllocation fastTrackAllocation = caseData.getFastTrackAllocation();
            String reasons = "";
            if (fastTrackAllocation != null) {
                reasons = getFastTrackAllocationReason(fastTrackAllocation, reasons);
                if (NO.equals(fastTrackAllocation.getAssignComplexityBand())) {
                    return String.format("%s%s%s", FAST_TRACK_ALLOCATION_BASE, FAST_TRACK_ALLOCATION_NO_COMPLEXITY, reasons);
                } else if (YES.equals(fastTrackAllocation.getAssignComplexityBand())) {
                    String band = String.format(
                        FAST_TRACK_ALLOCATION_WTIH_COMPLEXITY,
                        fastTrackAllocation.getBand().getLabel().toLowerCase()
                    );
                    return String.format("%s%s%s", FAST_TRACK_ALLOCATION_BASE, band, reasons);
                }
            }
        }
        return "";
    }

    private static String getFastTrackAllocationReason(FastTrackAllocation fastTrackAllocation, String reasons) {
        if (fastTrackAllocation.getReasons() != null
            && !fastTrackAllocation.getReasons().equals("")) {
            reasons = String.format(FAST_TRACK_ALLOCATION_REASON, fastTrackAllocation.getReasons());
        }
        return reasons;
    }

    public static String getDisposalHearingFinalDisposalHearingTimeLabel(CaseData caseData) {
        DisposalHearingFinalDisposalHearing disposalHearingFinalDisposalHearing =
            caseData.getDisposalHearingFinalDisposalHearing();

        if (disposalHearingFinalDisposalHearing != null) {
            return disposalHearingFinalDisposalHearing.getTime().getLabel();
        }

        return "";
    }

    public static String getDisposalHearingMethodTelephoneHearingLabel(CaseData caseData) {
        DisposalHearingMethodTelephoneHearing disposalHearingMethodTelephoneHearing =
            caseData.getDisposalHearingMethodTelephoneHearing();

        if (disposalHearingMethodTelephoneHearing != null) {
            return disposalHearingMethodTelephoneHearing.getLabel();
        }

        return "";
    }

    public static String getDisposalHearingMethodVideoConferenceHearingLabel(CaseData caseData) {
        DisposalHearingMethodVideoConferenceHearing disposalHearingMethodVideoConferenceHearing =
            caseData.getDisposalHearingMethodVideoConferenceHearing();

        if (disposalHearingMethodVideoConferenceHearing != null) {
            return disposalHearingMethodVideoConferenceHearing.getLabel();
        }

        return "";
    }

    public static String getDisposalHearingBundleTypeText(CaseData caseData) {
        DisposalHearingBundle disposalHearingBundle = caseData.getDisposalHearingBundle();

        if (disposalHearingBundle != null) {
            List<DisposalHearingBundleType> types = disposalHearingBundle.getType();
            StringBuilder stringBuilder = new StringBuilder();

            if (disposalHearingBundle.getType().size() == 3) {
                stringBuilder.append(DisposalHearingBundleType.DOCUMENTS.getLabel());
                stringBuilder.append(" / " + DisposalHearingBundleType.ELECTRONIC.getLabel());
                stringBuilder.append(" / " + DisposalHearingBundleType.SUMMARY.getLabel());
            } else if (disposalHearingBundle.getType().size() == 2) {
                stringBuilder.append(types.get(0).getLabel());
                stringBuilder.append(" / " + types.get(1).getLabel());
            } else {
                stringBuilder.append(types.get(0).getLabel());
            }

            return stringBuilder.toString();
        }

        return "";
    }

    public static boolean hasDisposalVariable(CaseData caseData, String variableName) {
        switch (variableName) {
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
                return caseData.getDisposalHearingMethodToggle() != null;
            case "disposalHearingBundleToggle":
                return caseData.getDisposalHearingBundleToggle() != null;
            case "disposalHearingClaimSettlingToggle":
                return caseData.getDisposalHearingClaimSettlingToggle() != null;
            case "disposalHearingCostsToggle":
                return caseData.getDisposalHearingCostsToggle() != null;
            case "disposalHearingAddNewDirections":
                return caseData.getDisposalHearingAddNewDirections() != null;
            case "disposalHearingDateToToggle":
                return caseData.getTrialHearingTimeDJ() != null
                    && caseData.getTrialHearingTimeDJ().getDateToToggle() != null;
            default:
                return false;
        }
    }
}
