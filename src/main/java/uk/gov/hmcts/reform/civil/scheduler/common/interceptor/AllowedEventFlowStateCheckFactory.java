package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.flowstate.AllowedEventService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;

/**
 * Factory for creating flow state verification interceptors for Civil claim scheduled tasks.
 *
 * <p>Operates exclusively on Civil claims ({@link uk.gov.hmcts.reform.civil.model.CaseData}).
 */
@Component
@RequiredArgsConstructor
public class AllowedEventFlowStateCheckFactory {

    private final AllowedEventService allowedEventService;
    private final IStateFlowEngine stateFlowEngine;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;

    /**
     * Creates an {@link AllowedEventFlowStateCheck} interceptor configured for the given {@link CaseEvent}.
     *
     * @param event the event to check against the current flow state of the Civil claim
     * @return a scheduler interceptor for flow state verification
     */
    public SchedulerInterceptor<CaseDetails> forEvent(CaseEvent event) {
        return new AllowedEventFlowStateCheck(
            allowedEventService,
            stateFlowEngine,
            caseDetailsConverter,
            coreCaseDataService,
            event
        );
    }
}
