package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.flowstate.AllowedEventService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;

@Component
@RequiredArgsConstructor
public class AllowedEventFlowStateCheckFactory {

    private final AllowedEventService allowedEventService;
    private final IStateFlowEngine stateFlowEngine;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;

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
