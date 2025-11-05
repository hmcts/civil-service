package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFeatureToggleService;
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
    private SdoFeatureToggleService featureToggleService;
    @Mock
    private SdoOrderDetailsService orderDetailsService;

    @Test
    void shouldReturnErrorWhenDisposalHearingNotAllowed() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .allocatedTrack(AllocatedTrack.MULTI_CLAIM)
            .orderType(OrderType.DISPOSAL)
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .build();

        CallbackParams params = CallbackParams.builder()
            .params(Map.of(BEARER_TOKEN, "token"))
            .build();
        SdoTaskContext context = new SdoTaskContext(caseData, params, SdoLifecycleStage.ORDER_DETAILS);

        SdoOrderDetailsTask task = new SdoOrderDetailsTask(featureToggleService, orderDetailsService);

        SdoTaskResult result = task.execute(context);

        assertThat(result.errors()).isEqualTo(List.of(ERROR_MINTI_DISPOSAL_NOT_ALLOWED));
        assertThat(result.updatedCaseData()).isEqualTo(caseData);
        verify(orderDetailsService, never()).updateOrderDetails(any());
    }

    @Test
    void shouldUpdateCaseDataWhenAllowed() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(false);

        CaseData original = CaseData.builder().build();
        CaseData updated = CaseData.builder().legacyCaseReference("updated").build();
        when(orderDetailsService.updateOrderDetails(any())).thenReturn(updated);

        CallbackParams params = CallbackParams.builder()
            .params(Collections.emptyMap())
            .build();
        SdoTaskContext context = new SdoTaskContext(original, params, SdoLifecycleStage.ORDER_DETAILS);

        SdoOrderDetailsTask task = new SdoOrderDetailsTask(featureToggleService, orderDetailsService);

        SdoTaskResult result = task.execute(context);

        assertThat(result.errors()).isEmpty();
        assertThat(result.updatedCaseData()).isEqualTo(updated);
        verify(orderDetailsService).updateOrderDetails(context);
    }

    @Test
    void shouldSupportOrderDetailsStageOnly() {
        SdoOrderDetailsTask task = new SdoOrderDetailsTask(featureToggleService, orderDetailsService);

        assertThat(task.supports(SdoLifecycleStage.ORDER_DETAILS)).isTrue();
        assertThat(task.supports(SdoLifecycleStage.MID_EVENT)).isFalse();
    }
}
