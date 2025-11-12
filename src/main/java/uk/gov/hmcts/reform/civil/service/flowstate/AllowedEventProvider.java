package uk.gov.hmcts.reform.civil.service.flowstate;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;

/**
 * Temporary interface for allowed events during refactoring.
 */
public interface AllowedEventProvider {
    /**
     * @deprecated use {@link AllowedEventProvider#isAllowed(CaseData, CaseEvent)}
     */
    @Deprecated
    boolean isAllowed(CaseDetails caseDetails, CaseEvent event);

    boolean isAllowed(CaseData caseData, CaseEvent event);
}
