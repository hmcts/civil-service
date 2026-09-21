package uk.gov.hmcts.reform.civil.scheduler.gaordermade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAApproveConsentOrder;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.Time;

@Component
@RequiredArgsConstructor
public class GAOrderMadeStayDeadlineFilter {

    private final Time time;
    private final CaseDetailsConverter caseDetailsConverter;

    public boolean hasExpiredStayDeadline(CaseDetails caseDetails) {
        GeneralApplicationCaseData caseData = caseDetailsConverter.toGeneralApplicationCaseData(caseDetails);
        return isJudgeOrderStayDeadlineExpired(caseData) || isConsentOrderStayDeadlineExpired(caseData);
    }

    private boolean isJudgeOrderStayDeadlineExpired(GeneralApplicationCaseData caseData) {
        GAJudicialMakeAnOrder judicialDecisionMakeOrder = caseData.getJudicialDecisionMakeOrder();
        return judicialDecisionMakeOrder != null
            && judicialDecisionMakeOrder.getJudgeApproveEditOptionDate() != null
            && !time.now().toLocalDate().isBefore(judicialDecisionMakeOrder.getJudgeApproveEditOptionDate());
    }

    private boolean isConsentOrderStayDeadlineExpired(GeneralApplicationCaseData caseData) {
        GAApproveConsentOrder approveConsentOrder = caseData.getApproveConsentOrder();
        return approveConsentOrder != null
            && approveConsentOrder.getConsentOrderDateToEnd() != null
            && !time.now().toLocalDate().isBefore(approveConsentOrder.getConsentOrderDateToEnd());
    }
}
