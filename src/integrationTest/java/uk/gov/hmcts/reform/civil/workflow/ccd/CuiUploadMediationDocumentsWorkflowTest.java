package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.MediationWorkflowFixtures;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.CLAIMANT;

@SuppressWarnings({"java:S5960", "java:S6813"})
class CuiUploadMediationDocumentsWorkflowTest extends WorkflowIntegrationTest {

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    void shouldReturnCaseDataWithoutErrorsOnAboutToSubmit() throws Exception {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
            .thenReturn(List.of(CLAIMANT.getFormattedName()));

        CaseData fixture = MediationWorkflowFixtures.inMediation1v1().toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .build();

        startWorkflow(fixture)
            .eventId(CaseEvent.CUI_UPLOAD_MEDIATION_DOCUMENTS)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getData()).isNotNull();
            });
    }

    @Test
    void shouldReturnCaseDataWithoutErrorsForDefendantRole() throws Exception {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
            .thenReturn(List.of(uk.gov.hmcts.reform.civil.enums.CaseRole.DEFENDANT.getFormattedName()));

        CaseData fixture = MediationWorkflowFixtures.inMediation1v1().toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .build();

        startWorkflow(fixture)
            .eventId(CaseEvent.CUI_UPLOAD_MEDIATION_DOCUMENTS)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getData()).isNotNull();
            });
    }
}
