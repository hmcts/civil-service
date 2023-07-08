package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import uk.gov.hmcts.reform.civil.enums.hearing.CategoryType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.hearingvalues.CaseCategoryModel;
import uk.gov.hmcts.reform.civil.service.hearings.CaseCategoriesService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagUtils;
import uk.gov.hmcts.reform.civil.utils.CaseNameUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ServiceHearingsCaseLevelMapper {

    public static final String CASE_DETAILS_URL = "%s/cases/case-details/%s";
    public static final String EMPTY_STRING = "";
    private static String DATE_FORMAT = "yyyy-MM-dd";

    private ServiceHearingsCaseLevelMapper() {
        //NO-OP
    }

    public static String getHmctsInternalCaseName(CaseData caseData) {
        return caseData.getCaseNameHmctsInternal();
    }

    public static String getPublicCaseName(CaseData caseData) {
        return caseData.getCaseNamePublic() != null ? caseData.getCaseNamePublic()
            : CaseNameUtils.buildCaseNamePublic(caseData);
    }

    public static String getCaseDeepLink(Long caseId,
                                         String baseUrl) {
        return String.format(CASE_DETAILS_URL, baseUrl, caseId);
    }

    public static boolean getCaseRestrictedFlag() {
        return false;
    }

    public static String getExternalCaseReference() {
        return null;
    }

    public static boolean getAutoListFlag() {
        return false;
    }

    public static String getCaseManagementLocationCode(CaseData caseData) {
        return caseData.getCaseManagementLocation().getBaseLocation();
    }

    public static String getCaseSLAStartDate(CaseData caseData) {
        if (caseData.getIssueDate() != null) {
            return caseData.getIssueDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        }
        return caseData.getSubmittedDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    public static boolean getCaseAdditionalSecurityFlag(CaseData caseData) {
        return CaseFlagsToHearingValueMapper.getAdditionalSecurity(CaseFlagUtils.getAllCaseFlags(caseData));
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
