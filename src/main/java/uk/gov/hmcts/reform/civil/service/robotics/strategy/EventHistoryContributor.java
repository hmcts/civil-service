package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;

/**
 * Pluggable component that contributes robotics events for a specific feature/flow.
 */
public interface EventHistoryContributor {

    /**
     * Returns true if this contributor should run for the supplied case data.
     */
    boolean supports(CaseData caseData);

    /**
     * Contributes events to the supplied builder. Implementations may use the auth token if needed.
     */
    void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken);
}
