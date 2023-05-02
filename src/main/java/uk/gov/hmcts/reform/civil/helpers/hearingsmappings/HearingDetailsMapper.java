package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.hearings.hearingrequest.model.HearingLocationModel;
import uk.gov.hmcts.reform.hearings.hearingrequest.model.HearingWindowModel;
import uk.gov.hmcts.reform.hearings.hearingrequest.model.JudiciaryModel;
import uk.gov.hmcts.reform.hearings.hearingrequest.model.PanelRequirementsModel;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsHearingsUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.hearings.hearingrequest.model.HMCLocationType.COURT;

public class HearingDetailsMapper {

    public static final String WELSH_REGION_ID = "7";
    public static final String STANDARD_PRIORITY = "Standard";
    public static final String SECURE_DOCK_KEY = "11";
    private static String EMPTY_STRING = "";

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
        return EMPTY_STRING;
        //todo CIV-6855
    }

    public static String getHearingRequester() {
        return EMPTY_STRING;
    }

    public static boolean getPrivateHearingRequiredFlag() {
        return false;
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
