package uk.gov.hmcts.reform.civil.service.flowstate;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

/**
 * Temporary interface for allowed events during refactoring.
 */
public interface AllowedEvents {
    boolean isAllowed(CaseDetails caseDetails, CaseEvent event);
}
