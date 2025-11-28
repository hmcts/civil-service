package uk.gov.hmcts.reform.civil.service.sdo;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingOnRadioOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2TimeEstimate;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing;

@Service
public class SdoR2SmallClaimsDirectionsService {

    private static final String MINUTES = " minutes";

    public boolean hasHearingTrialWindow(CaseData caseData) {
        return caseData.getSdoR2SmallClaimsHearing() != null
            && HearingOnRadioOptions.HEARING_WINDOW
            .equals(caseData.getSdoR2SmallClaimsHearing().getTrialOnOptions());
    }

    public String getPhysicalTrialBundleText(CaseData caseData) {
        SdoR2SmallClaimsHearing hearing = caseData.getSdoR2SmallClaimsHearing();
        if (hearing == null || hearing.getPhysicalBundleOptions() == null) {
            return "";
        }

        if (SmallClaimsSdoR2PhysicalTrialBundleOptions.NO.equals(hearing.getPhysicalBundleOptions())) {
            return "None";
        } else if (SmallClaimsSdoR2PhysicalTrialBundleOptions.PARTY.equals(hearing.getPhysicalBundleOptions())
            && hearing.getSdoR2SmallClaimsBundleOfDocs() != null) {
            return hearing.getSdoR2SmallClaimsBundleOfDocs().getPhysicalBundlePartyTxt();
        }

        return "";
    }

    public String getHearingTime(CaseData caseData) {
        SdoR2SmallClaimsHearing hearing = caseData.getSdoR2SmallClaimsHearing();
        if (hearing == null || hearing.getLengthList() == null) {
            return "";
        }

        if (hearing.getLengthList() == SmallClaimsSdoR2TimeEstimate.FIFTEEN_MINUTES) {
            return SmallClaimsSdoR2TimeEstimate.FIFTEEN_MINUTES.getLabel();
        } else if (hearing.getLengthList() == SmallClaimsSdoR2TimeEstimate.THIRTY_MINUTES) {
            return SmallClaimsSdoR2TimeEstimate.THIRTY_MINUTES.getLabel();
        } else if (hearing.getLengthList() == SmallClaimsSdoR2TimeEstimate.ONE_HOUR) {
            return SmallClaimsSdoR2TimeEstimate.ONE_HOUR.getLabel();
        } else if (hearing.getLengthList() == SmallClaimsSdoR2TimeEstimate.OTHER) {
            return hearing.getLengthListOther().getTrialLengthDays() + " days, "
                + hearing.getLengthListOther().getTrialLengthHours() + " hours, "
                + hearing.getLengthListOther().getTrialLengthMinutes() + MINUTES;
        }

        return "";
    }

    public String getHearingMethod(CaseData caseData) {
        SdoR2SmallClaimsHearing hearing = caseData.getSdoR2SmallClaimsHearing();
        if (hearing == null
            || hearing.getMethodOfHearing() == null
            || hearing.getMethodOfHearing().getValue() == null) {
            return "";
        }

        String label = hearing.getMethodOfHearing().getValue().getLabel();
        if (HearingMethod.TELEPHONE.getLabel().equals(label)) {
            return "by telephone";
        } else if (HearingMethod.VIDEO.getLabel().equals(label)) {
            return "by video conference";
        } else if (HearingMethod.IN_PERSON.getLabel().equals(label)) {
            return "in person";
        }

        return "";
    }

    public DynamicList getHearingLocation(CaseData caseData) {
        SdoR2SmallClaimsHearing hearing = caseData.getSdoR2SmallClaimsHearing();
        if (hearing == null) {
            return null;
        }

        if (hearing.getHearingCourtLocationList() != null
            && hearing.getHearingCourtLocationList().getValue() != null
            && !"OTHER_LOCATION".equals(hearing.getHearingCourtLocationList().getValue().getCode())) {
            return hearing.getHearingCourtLocationList();
        } else if (hearing.getAltHearingCourtLocationList() != null
            && hearing.getAltHearingCourtLocationList().getValue() != null) {
            return hearing.getAltHearingCourtLocationList();
        }

        return null;
    }
}
