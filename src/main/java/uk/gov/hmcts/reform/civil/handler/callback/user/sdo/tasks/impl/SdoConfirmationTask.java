package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskResult;
import uk.gov.hmcts.reform.civil.service.sdo.SdoNarrativeService;

@Component
@RequiredArgsConstructor
public class SdoConfirmationTask implements SdoCallbackTask {

    private final SdoNarrativeService sdoNarrativeService;

    @Override
    public SdoTaskResult execute(SdoTaskContext context) {
        String header = sdoNarrativeService.buildConfirmationHeader(context.caseData());
        String body = sdoNarrativeService.buildConfirmationBody(context.caseData());
        SubmittedCallbackResponse response = SubmittedCallbackResponse.builder()
            .confirmationHeader(header)
            .confirmationBody(body)
            .build();
        return SdoTaskResult.withSubmittedResponse(response);
    }

    @Override
    public boolean supports(SdoLifecycleStage stage) {
        return SdoLifecycleStage.CONFIRMATION == stage;
    }
}
