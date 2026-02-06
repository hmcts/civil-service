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
import uk.gov.hmcts.reform.civil.service.sdo.SdoSubmissionService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@Component
@RequiredArgsConstructor
@Slf4j
public class SdoSubmissionTask implements DirectionsOrderCallbackTask {

    private final SdoSubmissionService submissionService;

    @Override
    public DirectionsOrderTaskResult execute(DirectionsOrderTaskContext context) {
        CaseData caseData = context.caseData();
        String authToken = context.callbackParams().getParams().get(BEARER_TOKEN).toString();
        log.info("Preparing SDO submission for caseId {}", caseData.getCcdCaseReference());
        CaseData updatedCaseData = submissionService.prepareSubmission(caseData, authToken);
        return DirectionsOrderTaskResult.empty(updatedCaseData);
    }

    @Override
    public boolean supports(DirectionsOrderLifecycleStage stage) {
        return DirectionsOrderLifecycleStage.SUBMISSION == stage;
    }

    @Override
    public boolean appliesTo(DirectionsOrderTaskContext context) {
        return DirectionsOrderTaskSupport.supportsEvent(context, CREATE_SDO);
    }
}
