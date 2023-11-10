package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.hearingvalues.HearingLocationModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.HearingWindowModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.JudiciaryModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.PanelRequirementsModel;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsHearingsUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.enums.hearing.HMCLocationType.COURT;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagsHearingsUtils.getAllActiveFlags;
import static uk.gov.hmcts.reform.civil.utils.DynamicListUtils.getDynamicListValue;
import static uk.gov.hmcts.reform.civil.utils.HearingMethodUtils.getHearingMethodCodes;

public class HearingDetailsMapper {

    public static final String WELSH_REGION_ID = "7";
    public static final String STANDARD_PRIORITY = "Standard";
    public static final String SECURE_DOCK_KEY = "11";
    private static String EMPTY_STRING = "";

    private static String AUDIO_VIDEO_EVIDENCE_FLAG = "PF0014";

    private HearingDetailsMapper() {
        //NO-OP
    }

    public static String getHearingType() {
        return null;
    }

    public static HearingWindowModel getHearingWindow() {
        return null;
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

    public static boolean getHearingInWelshFlag(CaseData caseData) {
        return isHearingInWales(caseData) && isWelshHearingSelected(caseData);
    }

    private static boolean isHearingInWales(CaseData caseData) {
        if (Objects.nonNull(caseData.getCaseManagementLocation()) && Objects.nonNull(caseData.getCaseManagementLocation()
                                                                                         .getRegion())) {
            return caseData.getCaseManagementLocation().getRegion().equals(WELSH_REGION_ID);
        } else {
            return false;
        }
    }

    private static boolean isWelshHearingSelected(CaseData caseData) {
        List<Language> welshLanguageRequirements = getWelshLanguageRequirements(caseData);

        return (welshLanguageRequirements.contains(Language.WELSH) || welshLanguageRequirements.contains(Language.BOTH));
    }

    private static List<Language> getWelshLanguageRequirements(CaseData caseData) {
        List<Language> welshLanguageRequirements = new ArrayList<>();
        if (Objects.nonNull(caseData.getRespondent1DQ()) && Objects.nonNull(caseData.getRespondent1DQ()
                                                                                .getWelshLanguageRequirements())) {
            welshLanguageRequirements.add(caseData.getRespondent1DQ().getWelshLanguageRequirements().getCourt());
        }

        if (Objects.nonNull(caseData.getRespondent2DQ()) && Objects.nonNull(caseData.getRespondent2DQ()
                                                                                .getWelshLanguageRequirements())) {
            welshLanguageRequirements.add(caseData.getRespondent2DQ().getWelshLanguageRequirements().getCourt());
        }

        if (Objects.nonNull(caseData.getApplicant1DQ()) && Objects.nonNull(caseData.getApplicant1DQ()
                                                                               .getWelshLanguageRequirements())) {
            welshLanguageRequirements.add(caseData.getApplicant1DQ().getWelshLanguageRequirements().getCourt());
        }

        return welshLanguageRequirements;
    }

    public static List<HearingLocationModel> getHearingLocations(CaseData caseData) {
        HearingLocationModel hearingLocationModel = HearingLocationModel.builder()
            .locationId(caseData.getCaseManagementLocation().getBaseLocation())
            .locationType(COURT)
            .build();
        return List.of(hearingLocationModel);
    }

    public static List<String> getFacilitiesRequired(CaseData caseData) {
        if (CaseFlagsHearingsUtils.detainedIndividualFlagExist(caseData)) {
            return List.of(SECURE_DOCK_KEY);
        }
        return null;
    }

    public static String getListingComments(CaseData caseData) {
        String comments = getAllActiveFlags(caseData).stream()
            .flatMap(flags -> flags.getDetails().stream())
            .filter(flag -> flag.getValue() != null && flag.getValue().getFlagCode().equals(AUDIO_VIDEO_EVIDENCE_FLAG))
            .map(flag -> String.format(flag.getValue().getFlagComment() == null ? "%s, " : "%s: %s, ", flag.getValue().getName(), flag.getValue().getFlagComment()))
            .reduce("", String::concat)
            .replaceAll("\n", " ")
            .replaceAll("\\s+", " ");

        if (comments != null && !comments.isEmpty()) {
            String refactoredComment = comments.substring(0, comments.length() - 2);
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

    public static PanelRequirementsModel getPanelRequirements() {
        return null;
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

    public static List<String> getHearingChannels(String authToken, String hmctsServiceId, CaseData caseData, CategoryService categoryService) {
        Map<String, String> hearingMethodCode = getHearingMethodCodes(categoryService, hmctsServiceId, authToken);
        if (caseData.getHearingMethodValuesFastTrack() != null) {
            return List.of(hearingMethodCode.get(getDynamicListValue(caseData.getHearingMethodValuesFastTrack())));
        } else if (caseData.getHearingMethodValuesDisposalHearing() != null) {
            return List.of(hearingMethodCode.get(getDynamicListValue(caseData.getHearingMethodValuesDisposalHearing())));
        } else if (caseData.getHearingMethodValuesDisposalHearingDJ() != null) {
            return List.of(hearingMethodCode.get(getDynamicListValue(caseData.getHearingMethodValuesDisposalHearingDJ())));
        } else if (caseData.getHearingMethodValuesTrialHearingDJ() != null) {
            return List.of(hearingMethodCode.get(getDynamicListValue(caseData.getHearingMethodValuesTrialHearingDJ())));
        } else if (caseData.getHearingMethodValuesSmallClaims() != null) {
            return List.of(hearingMethodCode.get(getDynamicListValue(caseData.getHearingMethodValuesSmallClaims())));
        } else {
            return null;
        }
    }

}
