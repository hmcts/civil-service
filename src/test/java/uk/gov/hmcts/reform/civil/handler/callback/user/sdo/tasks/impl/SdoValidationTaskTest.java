package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.sdo.SdoValidationService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SdoValidationTaskTest {

    @Mock
    private SdoValidationService validationService;
    @InjectMocks
    private SdoValidationTask task;

    @Test
    void shouldReturnErrorsFromService() {
        CaseData caseData = CaseDataBuilder.builder().build();
        when(validationService.validate(caseData)).thenReturn(List.of("validation-error"));

        CallbackParams params = new CallbackParams()
            .params(Collections.emptyMap());
        DirectionsOrderTaskContext context =
            new DirectionsOrderTaskContext(caseData, params, DirectionsOrderLifecycleStage.MID_EVENT);

        DirectionsOrderTaskResult result = task.execute(context);

        assertThat(result.updatedCaseData()).isEqualTo(caseData);
        assertThat(result.errors()).containsExactly("validation-error");
        assertThat(result.submittedCallbackResponse()).isNull();
        verify(validationService).validate(caseData);
    }

    @Test
    void shouldSupportMidEventStageOnly() {
        assertThat(task.supports(DirectionsOrderLifecycleStage.MID_EVENT)).isTrue();
        assertThat(task.supports(DirectionsOrderLifecycleStage.ORDER_DETAILS)).isFalse();
    }
}
