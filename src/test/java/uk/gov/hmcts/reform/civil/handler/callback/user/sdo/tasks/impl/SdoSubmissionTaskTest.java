package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFeatureToggleService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoLocationService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@ExtendWith(MockitoExtension.class)
class SdoSubmissionTaskTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private SdoFeatureToggleService featureToggleService;

    @Mock
    private SdoLocationService sdoLocationService;

    @Test
    void shouldUpdateWaLocationsDuringSubmission() {
        SdoSubmissionTask task = new SdoSubmissionTask(featureToggleService, sdoLocationService);
        CaseData caseData = CaseData.builder().build();
        CallbackParams params = CallbackParams.builder()
            .params(Map.of(BEARER_TOKEN, AUTH_TOKEN))
            .build();
        SdoTaskContext context = new SdoTaskContext(caseData, params, SdoLifecycleStage.SUBMISSION);

        SdoTaskResult result = task.execute(context);

        assertThat(result.updatedCaseData()).isEqualTo(caseData);
        assertThat(result.errors()).isEmpty();

        ArgumentCaptor<CaseData.CaseDataBuilder<?, ?>> builderCaptor = ArgumentCaptor.forClass(CaseData.CaseDataBuilder.class);
        verify(sdoLocationService).updateWaLocationsIfRequired(
            eq(caseData),
            builderCaptor.capture(),
            eq(AUTH_TOKEN)
        );
        assertThat(builderCaptor.getValue()).isNotNull();
    }

    @Test
    void shouldSupportSubmissionStageOnly() {
        SdoSubmissionTask task = new SdoSubmissionTask(featureToggleService, sdoLocationService);

        assertThat(task.supports(SdoLifecycleStage.SUBMISSION)).isTrue();
        assertThat(task.supports(SdoLifecycleStage.MID_EVENT)).isFalse();
    }
}
