package uk.gov.hmcts.reform.civil.helpers.sdo;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.*;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;

import java.util.List;

public class SdoHelper {

    public static boolean isSmallClaimsTrack (CaseData caseData) {
        YesOrNo drawDirectionsOrderRequired = caseData.getDrawDirectionsOrderRequired();
        YesOrNo drawDirectionsOrderSmallClaims = caseData.getDrawDirectionsOrderSmallClaims();
        ClaimsTrack claimsTrack = caseData.getClaimsTrack();

        Boolean smallClaimsPath1 = (drawDirectionsOrderRequired == YesOrNo.NO)
            && (claimsTrack == ClaimsTrack.smallClaimsTrack);
        Boolean smallClaimsPath2 = (drawDirectionsOrderRequired == YesOrNo.YES)
            && (drawDirectionsOrderSmallClaims == YesOrNo.YES);

        return smallClaimsPath1 || smallClaimsPath2;
    }

    public static boolean isFastTrack (CaseData caseData) {
        YesOrNo drawDirectionsOrderRequired = caseData.getDrawDirectionsOrderRequired();
        YesOrNo drawDirectionsOrderSmallClaims = caseData.getDrawDirectionsOrderSmallClaims();
        ClaimsTrack claimsTrack = caseData.getClaimsTrack();
        OrderType orderType = caseData.getOrderType();

        Boolean fastTrackPath1 = (drawDirectionsOrderRequired == YesOrNo.NO)
            && (claimsTrack == ClaimsTrack.fastTrack);
        Boolean fastTrackPath2 = (drawDirectionsOrderRequired == YesOrNo.YES)
            && (drawDirectionsOrderSmallClaims == YesOrNo.NO) && (orderType == OrderType.DECIDE_DAMAGES);

        return fastTrackPath1 || fastTrackPath2;
    }

    public static SmallTrack getSmallClaimsAdditionalDirectionEnum (String additionalDirection) {
        switch(additionalDirection) {
            case "smallClaimCreditHire":
                return SmallTrack.smallClaimCreditHire;
            case "smallClaimRoadTrafficAccident":
                return SmallTrack.smallClaimRoadTrafficAccident;
            default:
                return null;
        }
    }

    public static boolean hasSmallAdditionalDirections (CaseData caseData, String additionalDirection) {
        SmallTrack additionalDirectionEnum = getSmallClaimsAdditionalDirectionEnum(additionalDirection);
        List<SmallTrack> smallClaims = caseData.getSmallClaims();
        boolean hasDirection;

        System.out.println("small claims: " + caseData.getSmallClaims());

        if ((smallClaims != null) && (additionalDirectionEnum != null)) {
            hasDirection = (caseData.getDrawDirectionsOrderRequired() == YesOrNo.NO) &&
                (caseData.getClaimsTrack() == ClaimsTrack.smallClaimsTrack) &&
                (caseData.getSmallClaims().contains(additionalDirectionEnum));
        } else {
            hasDirection = false;
        }

        return hasDirection;
    }

    public static String getSmallClaimsHearingTimeLabel (CaseData caseData) {
        SmallClaimsHearing smallClaimHearing = caseData.getSmallClaimsHearing();

        if (smallClaimHearing != null) {
            return smallClaimHearing.getTime().getLabel();
        }

        return "";
    }

    public static String getSmallClaimsMethodTelephoneHearingLabel (CaseData caseData) {
        SmallClaimsMethodTelephoneHearing smallClaimsMethodTelephoneHearing = caseData.getSmallClaimsMethodTelephoneHearing();

        if (smallClaimsMethodTelephoneHearing != null) {
            return smallClaimsMethodTelephoneHearing.getLabel();
        }

        return "";
    }

    public static String getSmallClaimsMethodVideoConferenceHearingLabel (CaseData caseData) {
        SmallClaimsMethodVideoConferenceHearing smallClaimsMethodVideoConferenceHearing = caseData.getSmallClaimsMethodVideoConferenceHearing();

        if (smallClaimsMethodVideoConferenceHearing != null) {
            return smallClaimsMethodVideoConferenceHearing.getLabel();
        }

        return "";
    }

    public static boolean hasSharedVariable(CaseData caseData, String variableName) {
        switch(variableName) {
            case "applicant2":
                return caseData.getApplicant2() != null;
            case "respondent2":
                return caseData.getRespondent2() != null;
            default:
                return false;
        }
    }

    public static boolean hasSmallClaimsVariable(CaseData caseData, String variableName) {
        switch(variableName) {
            case "smallClaimsHearingToggle":
                return caseData.getSmallClaimsHearingToggle() != null;
            case "smallClaimsMethodToggle":
                return caseData.getSmallClaimsMethodToggle() != null;
            case "smallClaimsDocumentsToggle":
                return caseData.getSmallClaimsDocumentsToggle() != null;
            case "smallClaimsWitnessStatementToggle":
                return caseData.getSmallClaimsWitnessStatementToggle() != null;
            case "smallClaimsAddNewDirections":
                return caseData.getSmallClaimsAddNewDirections() != null;
            default:
                return false;
        }
    }
}
