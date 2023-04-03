package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.hearingvalues.HearingLocationModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.HearingWindowModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.JudiciaryModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.PanelRequirementsModel;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.enums.hearing.HMCLocationType.COURT;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagsHearingsUtils.getAllActiveFlags;

public class HearingDetailsMapper {

    private static String EMPTY_STRING = "";
    public static String STANDARD_PRIORITY = "Standard";

    public static String AUDIO_VIDEO_EVIDENCE_FLAG = "PF0014";

    private HearingDetailsMapper() {
        //NO-OP
    }

    public static String getHearingType() {
        return EMPTY_STRING;
    }

    public static HearingWindowModel getHearingWindow() {
        return HearingWindowModel.builder()
            .build();
    }

    public static Integer getDuration() {
        return 0;
    }

    public static String getHearingPriorityType() {
        return STANDARD_PRIORITY;
    }

    public static Integer getNumberOfPhysicalAttendees() {
        return 0;
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
        String comments = getAllActiveFlags(caseData).stream()
            .flatMap(flags -> flags.getDetails().stream())
            .filter(flag -> flag.getValue() != null && flag.getValue().getFlagCode().equals(AUDIO_VIDEO_EVIDENCE_FLAG))
            .map(flag -> String.format(flag.getValue().getFlagComment() == null ? "%s, " : "%s: %s, ", flag.getValue().getName(), flag.getValue().getFlagComment()))
            .reduce("", String::concat);

        if(comments != null && !comments.isEmpty()) {
            String refactoredComment = comments.substring(0, comments.length() -2);
            return refactoredComment.length() > 200 ? refactoredComment.substring(0, 200) : refactoredComment;
        }

        return null;
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
        return null; //todo civ-6261
    }

}
