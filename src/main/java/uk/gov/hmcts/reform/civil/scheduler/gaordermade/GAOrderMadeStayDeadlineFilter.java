package uk.gov.hmcts.reform.civil.scheduler.gaordermade;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.Time;

import java.util.Objects;
import java.util.function.Predicate;

@Component
public class GAOrderMadeStayDeadlineFilter {

    private final Time time;
    private final CaseDetailsConverter caseDetailsConverter;
    private final Predicate<GeneralApplicationCaseData> isJudgeOrderStayDeadlineExpired;
    private final Predicate<GeneralApplicationCaseData> isConsentOrderStayDeadlineExpired;

    public GAOrderMadeStayDeadlineFilter(Time time, CaseDetailsConverter caseDetailsConverter) {
        this.time = time;
        this.caseDetailsConverter = caseDetailsConverter;
        this.isJudgeOrderStayDeadlineExpired = this::isJudgeOrderStayDeadlineExpired;
        this.isConsentOrderStayDeadlineExpired = this::isConsentOrderStayDeadlineExpired;
    }

    public boolean hasExpiredStayDeadline(CaseDetails caseDetails) {
        GeneralApplicationCaseData caseData = caseDetailsConverter.toGeneralApplicationCaseData(caseDetails);
        return isJudgeOrderStayDeadlineExpired.or(isConsentOrderStayDeadlineExpired).test(caseData);
    }

    private boolean isJudgeOrderStayDeadlineExpired(GeneralApplicationCaseData caseData) {
        return caseData.getJudicialDecisionMakeOrder() != null
            && caseData.getJudicialDecisionMakeOrder().getJudgeApproveEditOptionDate() != null
            && !time.now().toLocalDate().isBefore(caseData.getJudicialDecisionMakeOrder()
                                                       .getJudgeApproveEditOptionDate());
    }

    private boolean isConsentOrderStayDeadlineExpired(GeneralApplicationCaseData caseData) {
        return caseData.getApproveConsentOrder() != null
            && Objects.nonNull(caseData.getApproveConsentOrder().getConsentOrderDateToEnd())
            && !time.now().toLocalDate().isBefore(caseData.getApproveConsentOrder().getConsentOrderDateToEnd());
    }
}
