package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;

public class JudgmentAdmissionUtils {

    private JudgmentAdmissionUtils() {
        //NO-OP
    }

    public static boolean getLIPJudgmentAdmission(CaseData caseData) {
        return (caseData.isLipvLipOneVOne()
            && !caseData.isPayImmediately()
            && caseData.hasApplicantAcceptedRepaymentPlan()
            && caseData.isCcjRequestJudgmentByAdmission());
    }
}
