package uk.gov.hmcts.reform.civil.handler.callback.user.dj.tasks.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskResult;
import uk.gov.hmcts.reform.civil.service.dj.DjNarrativeService;

@Component
@RequiredArgsConstructor
public class DjConfirmationTask implements SdoCallbackTask {

    private final DjNarrativeService djNarrativeService;

    @Override
    public SdoTaskResult execute(SdoTaskContext context) {
        SubmittedCallbackResponse response = SubmittedCallbackResponse.builder()
            .confirmationHeader(djNarrativeService.buildConfirmationHeader(context.caseData()))
            .confirmationBody(djNarrativeService.buildConfirmationBody(context.caseData()))
            .build();
        return SdoTaskResult.withSubmittedResponse(response);
    }

    @Override
    public boolean supports(SdoLifecycleStage stage) {
        return SdoLifecycleStage.CONFIRMATION == stage;
    }
}
