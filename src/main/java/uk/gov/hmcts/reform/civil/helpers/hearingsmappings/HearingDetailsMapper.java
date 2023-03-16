package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.hearingvalues.HearingLocationModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.HearingWindowModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.JudiciaryModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.PanelRequirementsModel;

import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.hearing.HMCLocationType.COURT;

public class HearingDetailsMapper {

    private static String EMPTY_STRING = "";
    public static String STANDARD_PRIORITY = "Standard";

    private HearingDetailsMapper() {
        //NO-OP
    }

    public static String getHearingType() {
        return EMPTY_STRING;
    }

    public static HearingWindowModel getHearingWindow() {
        return HearingWindowModel.builder()
            .dateRangeEnd(EMPTY_STRING)
            .dateRangeStart(EMPTY_STRING)
            .firstDateTimeMustBe(EMPTY_STRING)
            .build();
    }

    public static Integer getDuration() {
        return 0;
    }

    public static String getHearingPriorityType() {
        return STANDARD_PRIORITY;
    }

    public static Integer getNumberOfPhysicalAttendees() {
        return null;
    }

    public static boolean getHearingInWelshFlag() {
        return false;
    }

    public static List<HearingLocationModel> getHearingLocations(CaseData caseData) {
        HearingLocationModel hearingLocationModel = HearingLocationModel.builder()
            .locationId(caseData.getCaseManagementLocation().getBaseLocation())
            .locationType(COURT)
            .build();
        return List.of(hearingLocationModel);
    }

    public static List<String> getFacilitiesRequired(CaseData caseData) {
        // todo civ-6888
        return null;
    }

    public static String getListingComments(CaseData caseData) {
        return EMPTY_STRING;
        //todo CIV-6855
    }

    public static String getHearingRequester() {
        return EMPTY_STRING;
    }

    public static boolean getPrivateHearingRequiredFlag() {
        return false;
    }

    public static boolean getCaseInterpreterRequiredFlag() {
        return false;
        // todo civ-6888
    }

    public static PanelRequirementsModel getPanelRequirements() {
        return PanelRequirementsModel.builder().build();
    }

    public static String getLeadJudgeContractType() {
        return EMPTY_STRING;
    }

    public static JudiciaryModel getJudiciary() {
        return JudiciaryModel.builder().build();
    }

    public static boolean getHearingIsLinkedFlag() {
        return false;
    }

    public static List<String> getHearingChannels(CaseData caseData) {
        return hearingMethodIdentifier(caseData) != null ? List.of(hearingMethodIdentifier(caseData).getValue().toString())
            : null;
    }

    private static DynamicList hearingMethodIdentifier(CaseData caseData) {
        if (caseData.getHearingMethodValuesFastTrack() != null) {
            return caseData.getHearingMethodValuesFastTrack();
        } else if (caseData.getHearingMethodValuesDisposalHearing() != null) {
            return caseData.getHearingMethodValuesDisposalHearing();
        } else if (caseData.getHearingMethodValuesDisposalHearingDJ() != null) {
            return caseData.getHearingMethodValuesDisposalHearingDJ();
        } else if (caseData.getHearingMethodValuesTrialHearingDJ() != null) {
            return caseData.getHearingMethodValuesTrialHearingDJ();
        } else if (caseData.getHearingMethodValuesSmallClaims() != null) {
            return caseData.getHearingMethodValuesSmallClaims();
        } else {
            return null;
        }
    }

}
