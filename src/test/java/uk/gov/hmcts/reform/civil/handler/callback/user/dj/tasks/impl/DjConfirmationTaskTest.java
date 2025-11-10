package uk.gov.hmcts.reform.civil.handler.callback.user.dj.tasks.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dj.DjNarrativeService;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DjConfirmationTaskTest {

    @Mock
    private DjNarrativeService narrativeService;
    @InjectMocks
    private DjConfirmationTask task;

    @Test
    void shouldBuildSubmittedResponse() {
        CaseData caseData = CaseData.builder().build();
        when(narrativeService.buildConfirmationHeader(caseData)).thenReturn("# Confirmed");
        when(narrativeService.buildConfirmationBody(caseData)).thenReturn("Body text");

        CallbackParams params = CallbackParams.builder()
            .params(Collections.emptyMap())
            .build();
        DirectionsOrderTaskContext context =
            new DirectionsOrderTaskContext(caseData, params, DirectionsOrderLifecycleStage.CONFIRMATION);

        DirectionsOrderTaskResult result = task.execute(context);

        assertThat(result.updatedCaseData()).isNull();
        assertThat(result.errors()).isEmpty();
        assertThat(result.submittedCallbackResponse()).isNotNull();
        assertThat(result.submittedCallbackResponse().getConfirmationHeader()).isEqualTo("# Confirmed");
        assertThat(result.submittedCallbackResponse().getConfirmationBody()).isEqualTo("Body text");
        verify(narrativeService).buildConfirmationHeader(caseData);
        verify(narrativeService).buildConfirmationBody(caseData);
    }

    @Test
    void shouldSupportConfirmationStageOnly() {
        assertThat(task.supports(DirectionsOrderLifecycleStage.CONFIRMATION)).isTrue();
        assertThat(task.supports(DirectionsOrderLifecycleStage.DOCUMENT_GENERATION)).isFalse();
    }
}
