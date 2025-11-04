package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFeatureToggleService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@ExtendWith(MockitoExtension.class)
class SdoMidToggleTaskTest {

    @Mock
    private SdoCaseClassificationService classificationService;

    @Mock
    private SdoFeatureToggleService featureToggleService;

    @Test
    void shouldReturnExistingCaseDataDuringMidEvent() {
        SdoMidToggleTask task = new SdoMidToggleTask(classificationService, featureToggleService);
        CaseData caseData = CaseData.builder().build();
        CallbackParams params = CallbackParams.builder()
            .params(Map.of(BEARER_TOKEN, "token"))
            .build();
        SdoTaskContext context = new SdoTaskContext(caseData, params, SdoLifecycleStage.MID_EVENT);

        SdoTaskResult result = task.execute(context);

        assertThat(result.updatedCaseData()).isEqualTo(caseData);
        assertThat(result.errors()).isEmpty();
        verifyNoInteractions(classificationService, featureToggleService);
    }

    @Test
    void shouldSupportMidEventStageOnly() {
        SdoMidToggleTask task = new SdoMidToggleTask(classificationService, featureToggleService);

        assertThat(task.supports(SdoLifecycleStage.MID_EVENT)).isTrue();
        assertThat(task.supports(SdoLifecycleStage.CONFIRMATION)).isFalse();
    }
}

