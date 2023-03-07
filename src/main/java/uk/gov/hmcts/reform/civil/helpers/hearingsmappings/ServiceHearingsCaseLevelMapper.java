package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import org.apache.ibatis.annotations.Case;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.hearingvalues.CaseCategoryModel;

import java.time.DayOfWeek;
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

    //todo
    public static String getCaseDeepLink(String caseReference, CaseData caseData) {
        return String.format(CASE_DETAILS_URL, "todo get exui url", caseReference);
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
        return false;
    }

    public static List<CaseCategoryModel> getCaseCategories(CaseData caseData) {
        return null;// todo
    }
}
