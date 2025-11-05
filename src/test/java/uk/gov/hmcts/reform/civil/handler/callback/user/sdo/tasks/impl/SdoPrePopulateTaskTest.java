package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskResult;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFeatureToggleService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoPrePopulateService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler.ERROR_MINTI_DISPOSAL_NOT_ALLOWED;

@ExtendWith(MockitoExtension.class)
class SdoPrePopulateTaskTest {

    @Mock
    private SdoFeatureToggleService featureToggleService;
    @Mock
    private SdoPrePopulateService prePopulateService;

    @Test
    void shouldReturnExistingCaseDataDuringPrePopulate() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(false);

        SdoPrePopulateTask task = new SdoPrePopulateTask(featureToggleService, prePopulateService);
        CaseData caseData = CaseData.builder().build();
        when(prePopulateService.prePopulate(any())).thenReturn(caseData);
        CallbackParams params = CallbackParams.builder()
            .params(Map.of(BEARER_TOKEN, "token"))
            .build();
        SdoTaskContext context = new SdoTaskContext(caseData, params, SdoLifecycleStage.PRE_POPULATE);

        SdoTaskResult result = task.execute(context);

        assertThat(result.updatedCaseData()).isEqualTo(caseData);
        assertThat(result.errors()).isEmpty();
        verify(prePopulateService).prePopulate(context);
    }

    @Test
    void shouldReturnErrorWhenMultiTrackEnabled() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .allocatedTrack(AllocatedTrack.MULTI_CLAIM)
            .build();

        CallbackParams params = CallbackParams.builder()
            .params(Map.of(BEARER_TOKEN, "token"))
            .build();
        SdoTaskContext context = new SdoTaskContext(caseData, params, SdoLifecycleStage.PRE_POPULATE);

        SdoPrePopulateTask task = new SdoPrePopulateTask(featureToggleService, prePopulateService);

        SdoTaskResult result = task.execute(context);

        assertThat(result.errors()).containsExactly(ERROR_MINTI_DISPOSAL_NOT_ALLOWED);
        verify(prePopulateService, never()).prePopulate(any());
    }

    @Test
    void shouldSupportPrePopulateStageOnly() {
        SdoPrePopulateTask task = new SdoPrePopulateTask(featureToggleService, prePopulateService);

        assertThat(task.supports(SdoLifecycleStage.PRE_POPULATE)).isTrue();
        assertThat(task.supports(SdoLifecycleStage.MID_EVENT)).isFalse();
    }
}
