package uk.gov.hmcts.reform.civil.handler.callback.user.dj.tasks.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dj.DjOrderDetailsService;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DjOrderDetailsTaskTest {

    @Mock
    private DjOrderDetailsService orderDetailsService;
    @InjectMocks
    private DjOrderDetailsTask task;

    @Test
    void shouldPopulateTrialDisposalScreen() {
        CaseData original = CaseDataBuilder.builder().build();
        CaseData updated = CaseDataBuilder.builder().build();
        updated.setLegacyCaseReference("updated");
        CallbackParams params = new CallbackParams()
            .params(Collections.emptyMap())
            .pageId("trial-disposal-screen");
        DirectionsOrderTaskContext context =
            new DirectionsOrderTaskContext(original, params, DirectionsOrderLifecycleStage.ORDER_DETAILS);
        when(orderDetailsService.populateTrialDisposalScreen(context)).thenReturn(updated);

        DirectionsOrderTaskResult result = task.execute(context);

        assertThat(result.updatedCaseData()).isEqualTo(updated);
        assertThat(result.errors()).isNull();
        verify(orderDetailsService).populateTrialDisposalScreen(context);
    }

    @Test
    void shouldApplyHearingSelectionsForCreateOrderPage() {
        CaseData original = CaseDataBuilder.builder().build();
        CaseData updated = CaseDataBuilder.builder().build();
        updated.setCcdCaseReference(123L);
        CallbackParams params = new CallbackParams()
            .params(Collections.emptyMap())
            .pageId("create-order")
            .version(CallbackVersion.V_2);
        DirectionsOrderTaskContext context =
            new DirectionsOrderTaskContext(original, params, DirectionsOrderLifecycleStage.ORDER_DETAILS);
        when(orderDetailsService.applyHearingSelections(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any()
        )).thenReturn(updated);

        DirectionsOrderTaskResult result = task.execute(context);

        verify(orderDetailsService).applyHearingSelections(original, CallbackVersion.V_2);
        assertThat(result.updatedCaseData()).isEqualTo(updated);
        assertThat(result.errors()).isNull();
    }

    @Test
    void shouldReturnOriginalCaseDataWhenPageNotHandled() {
        CaseData original = CaseDataBuilder.builder().build();
        original.setLegacyCaseReference("original");
        CallbackParams params = new CallbackParams()
            .params(Collections.emptyMap())
            .pageId("unknown-page");
        DirectionsOrderTaskContext context =
            new DirectionsOrderTaskContext(original, params, DirectionsOrderLifecycleStage.ORDER_DETAILS);

        DirectionsOrderTaskResult result = task.execute(context);

        assertThat(result.updatedCaseData()).isEqualTo(original);
        assertThat(result.errors()).isEmpty();
        verify(orderDetailsService, never()).populateTrialDisposalScreen(org.mockito.ArgumentMatchers.any());
        verify(orderDetailsService, never()).applyHearingSelections(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void shouldSupportOrderDetailsStageOnly() {
        assertThat(task.supports(DirectionsOrderLifecycleStage.ORDER_DETAILS)).isTrue();
        assertThat(task.supports(DirectionsOrderLifecycleStage.DOCUMENT_GENERATION)).isFalse();
    }
}
