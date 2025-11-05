package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.sdo.SdoNarrativeService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@ExtendWith(MockitoExtension.class)
class SdoConfirmationTaskTest {

    private static final String HEADER = "header";
    private static final String BODY = "body";

    @Mock
    private SdoNarrativeService narrativeService;

    @Test
    void shouldBuildSubmittedResponse() {
        SdoConfirmationTask task = new SdoConfirmationTask(narrativeService);
        CaseData caseData = CaseData.builder().build();
        CallbackParams params = CallbackParams.builder()
            .params(Map.of(BEARER_TOKEN, "token"))
            .build();
        DirectionsOrderTaskContext context =
            new DirectionsOrderTaskContext(caseData, params, DirectionsOrderLifecycleStage.CONFIRMATION);

        when(narrativeService.buildConfirmationHeader(caseData)).thenReturn(HEADER);
        when(narrativeService.buildConfirmationBody(caseData)).thenReturn(BODY);

        DirectionsOrderTaskResult result = task.execute(context);

        assertThat(result.submittedCallbackResponse()).isNotNull();
        assertThat(result.submittedCallbackResponse().getConfirmationHeader()).isEqualTo(HEADER);
        assertThat(result.submittedCallbackResponse().getConfirmationBody()).isEqualTo(BODY);
        verify(narrativeService).buildConfirmationHeader(caseData);
        verify(narrativeService).buildConfirmationBody(caseData);
    }

    @Test
    void shouldSupportConfirmationStageOnly() {
        SdoConfirmationTask task = new SdoConfirmationTask(narrativeService);

        assertThat(task.supports(DirectionsOrderLifecycleStage.CONFIRMATION)).isTrue();
        assertThat(task.supports(DirectionsOrderLifecycleStage.SUBMISSION)).isFalse();
}
}
