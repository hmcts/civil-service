package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;

public interface EventHistoryContributor {

    boolean supports(CaseData caseData);

    void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken);
}
