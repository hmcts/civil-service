package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import uk.gov.hmcts.reform.civil.enums.hearing.CategoryType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.hearingvalues.CaseCategoryModel;
import uk.gov.hmcts.reform.civil.service.hearings.CaseCategoriesService;

import java.util.ArrayList;
import java.util.List;

public class ServiceHearingsCaseLevelMapper {

    public static final String CASE_DETAILS_URL = "%s/cases/case-details/%s";
    public static final String EMPTY_STRING = "";

    private ServiceHearingsCaseLevelMapper() {
        //NO-OP
    }

    public static String getHmctsInternalCaseName(CaseData caseData) {
        return caseData.getCaseNameHmctsInternal();
    }

    public static String getPublicCaseName(CaseData caseData) {
        //todo civ-7030
        return null;
    }

    public static String getCaseDeepLink(Long caseId,
                                         String baseUrl) {
        return String.format(CASE_DETAILS_URL, baseUrl, caseId);
    }

    public static boolean getCaseRestrictedFlag() {
        return false;
    }

    public static String getExternalCaseReference() {
        return EMPTY_STRING;
    }

    public static boolean getAutoListFlag() {
        return false;
    }

    public static String getCaseManagementLocationCode(CaseData caseData) {
        return caseData.getCaseManagementLocation().getBaseLocation();
    }

    //todo civ-6854
    public static String getCaseSLAStartDate(CaseData caseData) {
        return "";
    }

    public static boolean getCaseAdditionalSecurityFlag() {
        // todo civ-6888
        return false;
    }

    public static List<CaseCategoryModel> getCaseCategories(CaseData caseData, CaseCategoriesService caseCategoriesService, String authToken) {
        ArrayList<CaseCategoryModel> caseCategories = new ArrayList<>();
        CaseCategoryModel caseType = caseCategoriesService.getCaseCategoriesFor(
            CategoryType.CASE_TYPE,
            caseData,
            authToken
        );
        if (caseType != null) {
            caseCategories.add(caseType);
        }
        CaseCategoryModel caseSubType = caseCategoriesService.getCaseCategoriesFor(
            CategoryType.CASE_SUBTYPE,
            caseData,
            authToken
        );
        if (caseSubType != null) {
            caseCategories.add(caseSubType);
        }
        return caseCategories;
    }
}
