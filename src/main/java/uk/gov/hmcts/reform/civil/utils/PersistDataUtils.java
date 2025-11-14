package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;

public class PersistDataUtils {

    private PersistDataUtils() {
        //NO-OP
    }

    public static CaseData persistPartyAddress(CaseData oldCaseData, CaseData caseData) {
        if (null != caseData.getApplicant1()
            && null == caseData.getApplicant1().getPrimaryAddress()
            && null != oldCaseData && null != oldCaseData.getApplicant1()
            && null != oldCaseData.getApplicant1().getPrimaryAddress()) {
            caseData.getApplicant1().setPrimaryAddress(oldCaseData.getApplicant1().getPrimaryAddress());
        }
        if (null != caseData.getRespondent1()
            && null == caseData.getRespondent1().getPrimaryAddress()
            && null != oldCaseData && null != oldCaseData.getRespondent1()
            && null != oldCaseData.getRespondent1().getPrimaryAddress()) {
            caseData.getRespondent1().setPrimaryAddress(oldCaseData.getRespondent1().getPrimaryAddress());
        }
        if (null != caseData.getApplicant2()
            && null == caseData.getApplicant2().getPrimaryAddress()
            && null != oldCaseData && null != oldCaseData.getApplicant2()
            && null != oldCaseData.getApplicant2().getPrimaryAddress()) {
            caseData.getApplicant2().setPrimaryAddress(oldCaseData.getApplicant2().getPrimaryAddress());
        }
        if (null != caseData.getRespondent2()
            && null == caseData.getRespondent2().getPrimaryAddress()
            && null != oldCaseData && null != oldCaseData.getRespondent2()
            && null != oldCaseData.getRespondent2().getPrimaryAddress()) {
            caseData.getRespondent2().setPrimaryAddress(oldCaseData.getRespondent2().getPrimaryAddress());
        }
        return caseData;
    }
}
