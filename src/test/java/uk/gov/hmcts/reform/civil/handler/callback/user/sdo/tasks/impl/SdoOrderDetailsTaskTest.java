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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.sdo.SdoDisposalGuardService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoOrderDetailsService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler.ERROR_MINTI_DISPOSAL_NOT_ALLOWED;

@ExtendWith(MockitoExtension.class)
class SdoOrderDetailsTaskTest {

    @Mock
    private SdoDisposalGuardService disposalGuardService;
    @Mock
    private SdoOrderDetailsService orderDetailsService;

    @Test
    void shouldReturnErrorWhenDisposalHearingNotAllowed() {
        when(disposalGuardService.shouldBlockOrderDetails(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();

        CallbackParams params = new CallbackParams()
            .params(Map.of(BEARER_TOKEN, "token"));
        DirectionsOrderTaskContext context =
            new DirectionsOrderTaskContext(caseData, params, DirectionsOrderLifecycleStage.ORDER_DETAILS);

        SdoOrderDetailsTask task = new SdoOrderDetailsTask(disposalGuardService, orderDetailsService);

        DirectionsOrderTaskResult result = task.execute(context);

        assertThat(result.errors()).isEqualTo(List.of(ERROR_MINTI_DISPOSAL_NOT_ALLOWED));
        assertThat(result.updatedCaseData()).isEqualTo(caseData);
        verify(orderDetailsService, never()).updateOrderDetails(any());
    }

    @Test
    void shouldUpdateCaseDataWhenAllowed() {
        when(disposalGuardService.shouldBlockOrderDetails(any())).thenReturn(false);

        CaseData original = CaseDataBuilder.builder().build();
        CaseData updated = CaseDataBuilder.builder().build();
        updated.setLegacyCaseReference("updated");
        when(orderDetailsService.updateOrderDetails(any())).thenReturn(updated);

        CallbackParams params = new CallbackParams()
            .params(Collections.emptyMap());
        DirectionsOrderTaskContext context =
            new DirectionsOrderTaskContext(original, params, DirectionsOrderLifecycleStage.ORDER_DETAILS);

        SdoOrderDetailsTask task = new SdoOrderDetailsTask(disposalGuardService, orderDetailsService);

        DirectionsOrderTaskResult result = task.execute(context);

        assertThat(result.errors()).isEmpty();
        assertThat(result.updatedCaseData()).isEqualTo(updated);
        verify(orderDetailsService).updateOrderDetails(context);
    }

    @Test
    void shouldSupportOrderDetailsStageOnly() {
        SdoOrderDetailsTask task = new SdoOrderDetailsTask(disposalGuardService, orderDetailsService);

        assertThat(task.supports(DirectionsOrderLifecycleStage.ORDER_DETAILS)).isTrue();
        assertThat(task.supports(DirectionsOrderLifecycleStage.MID_EVENT)).isFalse();
    }
}
