package uk.gov.hmcts.reform.civil.scheduler.gaunlessorder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.Time;

@Component
@RequiredArgsConstructor
public class GAUnlessOrderDeadlineFilter {

    private final Time time;
    private final CaseDetailsConverter caseDetailsConverter;

    public boolean hasExpiredUnlessOrderDeadline(CaseDetails caseDetails) {
        GeneralApplicationCaseData caseData = caseDetailsConverter.toGeneralApplicationCaseData(caseDetails);
        return isUnlessOrderDeadlineExpired(caseData);
    }

    private boolean isUnlessOrderDeadlineExpired(GeneralApplicationCaseData caseData) {
        GAJudicialMakeAnOrder judicialDecisionMakeOrder = caseData.getJudicialDecisionMakeOrder();
        return judicialDecisionMakeOrder != null
            && judicialDecisionMakeOrder.getJudgeApproveEditOptionDateForUnlessOrder() != null
            && YesOrNo.NO.equals(judicialDecisionMakeOrder.getIsOrderProcessedByUnlessScheduler())
            && !time.now().toLocalDate().isBefore(judicialDecisionMakeOrder.getJudgeApproveEditOptionDateForUnlessOrder());
    }
}
