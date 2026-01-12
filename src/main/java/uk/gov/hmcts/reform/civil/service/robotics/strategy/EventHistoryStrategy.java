package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

public interface EventHistoryStrategy {

    boolean supports(CaseData caseData);

    void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken);

    default void contribute(EventHistory.EventHistoryBuilder builder,
                            CaseData caseData,
                            String authToken,
                            FlowState.Main flowState) {
        contribute(builder, caseData, authToken);
    }
}
