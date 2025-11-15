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
import uk.gov.hmcts.reform.civil.service.sdo.SdoDisposalGuardService;
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
    private SdoDisposalGuardService disposalGuardService;
    @Mock
    private SdoPrePopulateService prePopulateService;

    @Test
    void shouldReturnExistingCaseDataDuringPrePopulate() {
        when(disposalGuardService.shouldBlockPrePopulate(any())).thenReturn(false);

        SdoPrePopulateTask task = new SdoPrePopulateTask(disposalGuardService, prePopulateService);
        CaseData caseData = CaseData.builder().build();
        when(prePopulateService.prePopulate(any())).thenReturn(caseData);
        CallbackParams params = CallbackParams.builder()
            .params(Map.of(BEARER_TOKEN, "token"))
            .build();
        DirectionsOrderTaskContext context =
            new DirectionsOrderTaskContext(caseData, params, DirectionsOrderLifecycleStage.PRE_POPULATE);

        DirectionsOrderTaskResult result = task.execute(context);

        assertThat(result.updatedCaseData()).isEqualTo(caseData);
        assertThat(result.errors()).isEmpty();
        verify(prePopulateService).prePopulate(context);
    }

    @Test
    void shouldReturnErrorWhenMultiTrackEnabled() {
        when(disposalGuardService.shouldBlockPrePopulate(any())).thenReturn(true);

        CaseData caseData = CaseData.builder().build();

        CallbackParams params = CallbackParams.builder()
            .params(Map.of(BEARER_TOKEN, "token"))
            .build();
        DirectionsOrderTaskContext context =
            new DirectionsOrderTaskContext(caseData, params, DirectionsOrderLifecycleStage.PRE_POPULATE);

        SdoPrePopulateTask task = new SdoPrePopulateTask(disposalGuardService, prePopulateService);

        DirectionsOrderTaskResult result = task.execute(context);

        assertThat(result.errors()).containsExactly(ERROR_MINTI_DISPOSAL_NOT_ALLOWED);
        verify(prePopulateService, never()).prePopulate(any());
    }

    @Test
    void shouldSupportPrePopulateStageOnly() {
        SdoPrePopulateTask task = new SdoPrePopulateTask(disposalGuardService, prePopulateService);

        assertThat(task.supports(DirectionsOrderLifecycleStage.PRE_POPULATE)).isTrue();
        assertThat(task.supports(DirectionsOrderLifecycleStage.MID_EVENT)).isFalse();
    }
}
