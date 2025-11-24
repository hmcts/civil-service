package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskSupport;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.sdo.SdoDisposalGuardService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoOrderDetailsService;

import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler.ERROR_MINTI_DISPOSAL_NOT_ALLOWED;

@Component
@RequiredArgsConstructor
@Slf4j
public class SdoOrderDetailsTask implements DirectionsOrderCallbackTask {

    private final SdoDisposalGuardService disposalGuardService;
    private final SdoOrderDetailsService sdoOrderDetailsService;

    @Override
    public DirectionsOrderTaskResult execute(DirectionsOrderTaskContext context) {
        CaseData caseData = context.caseData();
        log.info("SDO order details task for caseId {}", caseData.getCcdCaseReference());

        if (disposalGuardService.shouldBlockOrderDetails(caseData)) {
            log.info("Blocking disposal order details for caseId {} on multi/intermediate track", caseData.getCcdCaseReference());
            return DirectionsOrderTaskResult.withErrors(caseData, List.of(ERROR_MINTI_DISPOSAL_NOT_ALLOWED));
        }

        CaseData updatedCaseData = sdoOrderDetailsService.updateOrderDetails(context);

        return new DirectionsOrderTaskResult(updatedCaseData, Collections.emptyList(), null);
    }

    @Override
    public boolean supports(DirectionsOrderLifecycleStage stage) {
        return DirectionsOrderLifecycleStage.ORDER_DETAILS == stage;
    }

    @Override
    public boolean appliesTo(DirectionsOrderTaskContext context) {
        return DirectionsOrderTaskSupport.supportsEvent(context, CREATE_SDO);
    }

}
