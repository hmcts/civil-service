package uk.gov.hmcts.reform.civil.helpers.sdo;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingBundleType;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackTrialBundleType;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2TimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingOnRadioOptions;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingBundle;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearing;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackTrial;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class SdoHelper {

    public static final String EMPTY_STRING = "";
    public static final String BY_TELEPHONE = "by telephone";
    public static final String BY_VIDEO = "by video";
    public static final String IN_PERSON = "in person";

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

    public static boolean isNihlFastTrack(CaseData caseData) {

        return ((caseData.getDrawDirectionsOrderRequired() == NO
            && caseData.getFastClaims() != null
            && caseData.getFastClaims().contains(
            FastTrack.fastClaimNoiseInducedHearingLoss))
            || (caseData.getDrawDirectionsOrderRequired() == YES
            && caseData.getTrialAdditionalDirectionsForFastTrack() != null
            && caseData.getTrialAdditionalDirectionsForFastTrack()
            .contains(FastTrack.fastClaimNoiseInducedHearingLoss)));
    }

    public static DynamicList getHearingLocationNihl(CaseData caseData) {
        if (caseData.getSdoR2Trial().getHearingCourtLocationList() != null
            && caseData.getSdoR2Trial().getHearingCourtLocationList().getValue() != null
            && !caseData.getSdoR2Trial().getHearingCourtLocationList().getValue().getCode().equals("OTHER_LOCATION")) {
            return caseData.getSdoR2Trial().getHearingCourtLocationList();
        } else if (caseData.getSdoR2Trial().getAltHearingCourtLocationList() != null
            && caseData.getSdoR2Trial().getAltHearingCourtLocationList().getValue() != null) {
            return caseData.getSdoR2Trial().getAltHearingCourtLocationList();
        }
        return null;
    }

    public static String getPhysicalTrialTextNihl(CaseData caseData) {
        if (caseData.getSdoR2Trial() != null
            && PhysicalTrialBundleOptions.PARTY.equals(caseData.getSdoR2Trial().getPhysicalBundleOptions())) {
            return caseData.getSdoR2Trial().getPhysicalBundlePartyTxt();
        }
        return EMPTY_STRING;
    }

    public static boolean isRestrictWitnessNihl(CaseData caseData) {
        return caseData.getSdoR2WitnessesOfFact() != null
            && YesOrNo.YES.equals(caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness()
                                      .getIsRestrictWitness());
    }

    public static boolean isRestrictPagesNihl(CaseData caseData) {
        return caseData.getSdoR2WitnessesOfFact() != null
            && YesOrNo.YES.equals(caseData.getSdoR2WitnessesOfFact().getSdoRestrictPages()
                                      .getIsRestrictPages());
    }

    public static String isApplicationToRelyOnFurtherNihl(CaseData caseData) {
        return (caseData.getSdoR2QuestionsClaimantExpert() != null
            && YesOrNo.YES.equals(
            caseData.getSdoR2QuestionsClaimantExpert()
                .getSdoApplicationToRelyOnFurther().getDoRequireApplicationToRely())) ? "Yes" : "No";
    }

    public static boolean isClaimForPecuniaryLossNihl(CaseData caseData) {

        return (caseData.getSdoR2ScheduleOfLoss() != null
            && YesOrNo.YES.equals(caseData.getSdoR2ScheduleOfLoss().getIsClaimForPecuniaryLoss()));
    }

    public static  boolean isSDOR2ScreenForDRHSmallClaim(CaseData caseData) {
        return ((caseData.getDrawDirectionsOrderRequired() == NO
            && caseData.getSmallClaims() != null
            && caseData.getSmallClaims().contains(
            SmallTrack.smallClaimDisputeResolutionHearing))
            || (caseData.getDrawDirectionsOrderRequired() == YES
            && caseData.getDrawDirectionsOrderSmallClaimsAdditionalDirections() != null
            && caseData.getDrawDirectionsOrderSmallClaimsAdditionalDirections()
            .contains(SmallTrack.smallClaimDisputeResolutionHearing)));
    }

    public static DynamicList getHearingLocationDrh(CaseData caseData) {
        if (caseData.getSdoR2SmallClaimsHearing().getHearingCourtLocationList() != null
            && caseData.getSdoR2SmallClaimsHearing().getHearingCourtLocationList().getValue() != null
            && !caseData.getSdoR2SmallClaimsHearing().getHearingCourtLocationList().getValue().getCode()
            .equals("OTHER_LOCATION")) {
            return caseData.getSdoR2SmallClaimsHearing().getHearingCourtLocationList();
        } else if (caseData.getSdoR2SmallClaimsHearing().getAltHearingCourtLocationList() != null
            && caseData.getSdoR2SmallClaimsHearing().getAltHearingCourtLocationList().getValue() != null) {
            return caseData.getSdoR2SmallClaimsHearing().getAltHearingCourtLocationList();
        }
        return null;
    }

    public static boolean hasSdoR2HearingTrialWindow(CaseData caseData) {

        return caseData.getSdoR2SmallClaimsHearing() != null
            && HearingOnRadioOptions.HEARING_WINDOW.equals(caseData.getSdoR2SmallClaimsHearing().getTrialOnOptions());
    }

    static final String MINUTES = " minutes";

    public static String getSdoR2HearingTime(CaseData caseData) {

        if (caseData.getSdoR2SmallClaimsHearing() != null) {
            switch (caseData.getSdoR2SmallClaimsHearing().getLengthList()) {
                case FIFTEEN_MINUTES:
                    return SmallClaimsSdoR2TimeEstimate.FIFTEEN_MINUTES.getLabel();
                case THIRTY_MINUTES:
                    return SmallClaimsSdoR2TimeEstimate.THIRTY_MINUTES.getLabel();
                case ONE_HOUR:
                    return SmallClaimsSdoR2TimeEstimate.ONE_HOUR.getLabel();
                case OTHER:
                    return caseData.getSdoR2SmallClaimsHearing().getLengthListOther().getTrialLengthDays() + " days, "
                        + caseData.getSdoR2SmallClaimsHearing().getLengthListOther().getTrialLengthHours() + " hours, "
                        + caseData.getSdoR2SmallClaimsHearing().getLengthListOther().getTrialLengthMinutes() + MINUTES;
                default: return "";
            }
        }
        return "";
    }

    public static String getSdoR2SmallClaimsHearingMethod(CaseData caseData) {
        if (caseData.getSdoR2SmallClaimsHearing() != null) {
            if (HearingMethod.TELEPHONE.getLabel()
                .equals(caseData.getSdoR2SmallClaimsHearing().getMethodOfHearing().getValue().getLabel())) {
                return BY_TELEPHONE;
            } else if (HearingMethod.VIDEO.getLabel()
                .equals(caseData.getSdoR2SmallClaimsHearing().getMethodOfHearing().getValue().getLabel())) {
                return BY_VIDEO;
            } else if (HearingMethod.IN_PERSON.getLabel()
                .equals(caseData.getSdoR2SmallClaimsHearing().getMethodOfHearing().getValue().getLabel())) {
                return IN_PERSON;
            }
        }
        return "";
    }

    public static String getSdoR2SmallClaimsPhysicalTrialBundleTxt(CaseData caseData) {

        if (caseData.getSdoR2SmallClaimsHearing() != null) {
            if (SmallClaimsSdoR2PhysicalTrialBundleOptions.NO
                .equals(caseData.getSdoR2SmallClaimsHearing().getPhysicalBundleOptions())) {
                return "None";
            } else if (SmallClaimsSdoR2PhysicalTrialBundleOptions.PARTY
                .equals(caseData.getSdoR2SmallClaimsHearing().getPhysicalBundleOptions())) {
                return caseData.getSdoR2SmallClaimsHearing().getSdoR2SmallClaimsBundleOfDocs().getPhysicalBundlePartyTxt();
            }
        }
        return "";
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
            case "smallClaimDisputeResolutionHearing":
                return SmallTrack.smallClaimDisputeResolutionHearing;
            case "smallClaimFlightDelay":
                return SmallTrack.smallClaimFlightDelay;
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

    static final String OTHER = "Other";

    public static String getDisposalHearingTimeLabel(CaseData caseData) {
        DisposalHearingHearingTime disposalHearingHearingTime = caseData.getDisposalHearingHearingTime();

        String hearingTimeEstimateLabel = "";

        if (Optional.ofNullable(caseData.getDisposalHearingHearingTime())
            .map(DisposalHearingHearingTime::getTime)
            .map(DisposalHearingFinalDisposalHearingTimeEstimate::getLabel).isPresent()) {
            if (disposalHearingHearingTime.getTime().getLabel().equals(OTHER)) {
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
                        ? " minute" : MINUTES;
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
            if (smallClaimHearing.getTime().getLabel().equals(OTHER)) {
                StringBuilder otherLength = new StringBuilder();
                if (smallClaimHearing.getOtherHours() != null) {
                    otherLength.append(smallClaimHearing.getOtherHours().toString().trim() +
                                           " hours ");
                }
                if (smallClaimHearing.getOtherMinutes() != null) {
                    otherLength.append(smallClaimHearing.getOtherMinutes().toString().trim() + MINUTES);
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
            if (fastTrackHearingTime.getHearingDuration().getLabel().equals(OTHER)) {
                StringBuilder otherLength = new StringBuilder();
                if (fastTrackHearingTime.getOtherHours() != null) {
                    otherLength.append(fastTrackHearingTime.getOtherHours().trim() +
                                           " hours ");
                }
                if (fastTrackHearingTime.getOtherMinutes() != null) {
                    otherLength.append(fastTrackHearingTime.getOtherMinutes().trim() + MINUTES);
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

    public static boolean showCarmMediationSection(CaseData caseData, boolean carmEnabled) {
        return caseData.getSmallClaimsMediationSectionStatement() != null
            && caseData.getSmallClaimsMediationSectionStatement().getInput() != null
            && carmEnabled;
    }

    public static String getSmallClaimsMediationText(CaseData caseData) {
        if (caseData.getSmallClaimsMediationSectionStatement() != null) {
            return caseData.getSmallClaimsMediationSectionStatement().getInput();
        }
        return null;
    }

    public static boolean showCarmMediationSectionDRH(CaseData caseData, boolean carmEnabled) {
        return caseData.getSdoR2SmallClaimsMediationSectionStatement() != null
            && caseData.getSdoR2SmallClaimsMediationSectionStatement().getInput() != null
            && carmEnabled;
    }

    public static String getSmallClaimsMediationTextDRH(CaseData caseData) {
        if (caseData.getSdoR2SmallClaimsMediationSectionStatement() != null) {
            return caseData.getSdoR2SmallClaimsMediationSectionStatement().getInput();
        }
        return null;
    }

    public static boolean hasSmallClaimsVariable(CaseData caseData, String variableName) {
        switch (variableName) {
            case "smallClaimsHearingToggle":
                return caseData.getSmallClaimsHearingToggle() != null;
            case "smallClaimsMethodToggle":
                // SNI-5142
                return true;
            case "smallClaimsDocumentsToggle":
                return caseData.getSmallClaimsDocumentsToggle() != null;
            case "smallClaimsWitnessStatementToggle":
                return caseData.getSmallClaimsWitnessStatementToggle() != null;
            case "smallClaimsFlightDelayToggle":
                return caseData.getSmallClaimsFlightDelayToggle() != null;
            case "smallClaimsNumberOfWitnessesToggle":
                return caseData.getSmallClaimsWitnessStatement() != null
                    && caseData.getSmallClaimsWitnessStatement().getSmallClaimsNumberOfWitnessesToggle() != null;
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
                // SNI-5142
                return true;
            case "fastTrackAddNewDirections":
                return caseData.getFastTrackAddNewDirections() != null;
            case "fastTrackTrialDateToToggle":
                return caseData.getFastTrackHearingTime() != null
                    && caseData.getFastTrackHearingTime().getDateToToggle() != null;
            case "sdoR2FastTrackUseOfWelshToggle":
                return caseData.getSdoR2FastTrackUseOfWelshToggle() != null;
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
                // SNI-5142
                return true;
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

    public static String getSdoTrialHearingTimeAllocated(CaseData caseData) {

        if (caseData.getSdoR2Trial() != null) {
            if (caseData.getSdoR2Trial().getLengthList().getLabel().equals(OTHER)) {
                return caseData.getSdoR2Trial().getLengthListOther().getTrialLengthDays() + " days, "
                    + caseData.getSdoR2Trial().getLengthListOther().getTrialLengthHours() + " hours and "
                    + caseData.getSdoR2Trial().getLengthListOther().getTrialLengthMinutes() + MINUTES;
            } else {
                return caseData.getSdoR2Trial().getLengthList().getLabel();
            }
        }
        return "";
    }

    public static String getSdoTrialMethodOfHearing(CaseData caseData) {

        if (caseData.getSdoR2Trial() != null && caseData.getSdoR2Trial().getMethodOfHearing() != null) {
            if (HearingMethod.TELEPHONE.getLabel()
                .equals(caseData.getSdoR2Trial().getMethodOfHearing().getValue().getLabel())) {
                return BY_TELEPHONE;
            } else if (HearingMethod.VIDEO.getLabel()
                .equals(caseData.getSdoR2Trial().getMethodOfHearing().getValue().getLabel())) {
                return "by video conference";
            } else if (HearingMethod.IN_PERSON.getLabel()
                .equals(caseData.getSdoR2Trial().getMethodOfHearing().getValue().getLabel())) {
                return IN_PERSON;
            }
        }
        return "";
    }

}
