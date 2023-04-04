package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.hearingvalues.HearingLocationModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.HearingWindowModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.JudiciaryModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.PanelRequirementsModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.enums.hearing.HMCLocationType.COURT;

public class HearingDetailsMapper {

    private static String EMPTY_STRING = "";
    private static final String WELSH_REGION_ID = "7";
    public static String STANDARD_PRIORITY = "Standard";

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
        List<WelshLanguageRequirements> welshLanguageRequirements = getWelshLanguageRequirements(caseData);
        boolean isWelshHearing = false;

        for (int index = 0; index < welshLanguageRequirements.size() && !isWelshHearing; index++) {
            WelshLanguageRequirements requirements = welshLanguageRequirements.get(index);
            isWelshHearing = requirements.getCourt().equals(Language.WELSH) || requirements.getCourt().equals(Language.BOTH);
        }

        return isWelshHearing;
    }

    private static List<WelshLanguageRequirements> getWelshLanguageRequirements(CaseData caseData) {
        List<WelshLanguageRequirements> welshLanguageRequirements = new ArrayList<>();
        if (Objects.nonNull(caseData.getRespondent1DQ()) && Objects.nonNull(caseData.getRespondent1DQ()
                                                                                .getWelshLanguageRequirements())) {
            welshLanguageRequirements.add(caseData.getRespondent1DQ().getWelshLanguageRequirements());
        }

        if (Objects.nonNull(caseData.getRespondent2DQ()) && Objects.nonNull(caseData.getRespondent2DQ()
                                                                                .getWelshLanguageRequirements())) {
            welshLanguageRequirements.add(caseData.getRespondent2DQ().getWelshLanguageRequirements());
        }

        if (Objects.nonNull(caseData.getApplicant1DQ()) && Objects.nonNull(caseData.getApplicant1DQ()
                                                                               .getWelshLanguageRequirements())) {
            welshLanguageRequirements.add(caseData.getApplicant1DQ().getWelshLanguageRequirements());
        }

        if (Objects.nonNull(caseData.getApplicant2DQ()) && Objects.nonNull(caseData.getApplicant2DQ()
                                                                               .getWelshLanguageRequirements())) {
            welshLanguageRequirements.add(caseData.getApplicant2DQ().getWelshLanguageRequirements());
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
        return null; //todo civ-6261
    }

}
