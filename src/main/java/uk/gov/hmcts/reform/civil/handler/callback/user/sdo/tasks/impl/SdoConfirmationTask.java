package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskSupport;
import uk.gov.hmcts.reform.civil.service.sdo.SdoNarrativeService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;

@Component
@RequiredArgsConstructor
@Slf4j
public class SdoConfirmationTask implements DirectionsOrderCallbackTask {

    private final SdoNarrativeService sdoNarrativeService;

    @Override
    public DirectionsOrderTaskResult execute(DirectionsOrderTaskContext context) {
        log.info("Building SDO confirmation text for caseId {}", context.caseData().getCcdCaseReference());
        String header = sdoNarrativeService.buildConfirmationHeader(context.caseData());
        String body = sdoNarrativeService.buildConfirmationBody(context.caseData());
        SubmittedCallbackResponse response = SubmittedCallbackResponse.builder()
            .confirmationHeader(header)
            .confirmationBody(body)
            .build();
        return DirectionsOrderTaskResult.withSubmittedResponse(response);
    }

    @Override
    public boolean supports(DirectionsOrderLifecycleStage stage) {
        return DirectionsOrderLifecycleStage.CONFIRMATION == stage;
    }

    @Override
    public boolean appliesTo(DirectionsOrderTaskContext context) {
        return DirectionsOrderTaskSupport.supportsEvent(context, CREATE_SDO);
    }
}
