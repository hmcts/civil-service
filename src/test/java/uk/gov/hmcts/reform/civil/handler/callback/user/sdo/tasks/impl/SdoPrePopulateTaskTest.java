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
import uk.gov.hmcts.reform.civil.service.sdo.SdoPrePopulateService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@ExtendWith(MockitoExtension.class)
class SdoPrePopulateTaskTest {

    @Mock
    private SdoPrePopulateService prePopulateService;

    @Test
    void shouldReturnExistingCaseDataDuringPrePopulate() {
        SdoPrePopulateTask task = new SdoPrePopulateTask(prePopulateService);
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
    void shouldSupportPrePopulateStageOnly() {
        SdoPrePopulateTask task = new SdoPrePopulateTask(prePopulateService);

        assertThat(task.supports(DirectionsOrderLifecycleStage.PRE_POPULATE)).isTrue();
        assertThat(task.supports(DirectionsOrderLifecycleStage.MID_EVENT)).isFalse();
    }
}
