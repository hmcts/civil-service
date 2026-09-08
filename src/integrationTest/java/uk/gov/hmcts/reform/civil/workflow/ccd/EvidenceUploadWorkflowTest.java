package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_APPLICANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_RESPONDENT;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

@SuppressWarnings({"java:S5960", "java:S6813"})
class EvidenceUploadWorkflowTest extends WorkflowIntegrationTest {

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @BeforeEach
    void setUp() {
        when(userService.getUserInfo(anyString()))
            .thenReturn(UserInfo.builder()
                            .uid("solicitor-uid")
                            .sub("solicitor@example.com")
                            .roles(List.of("caseworker-civil-solicitor"))
                            .build());
    }

    @Test
    void shouldAcceptApplicantEvidenceUploadWithoutChangingState() throws Exception {
        CaseData fixture = caseProgressionFixture();

        startWorkflow(fixture)
            .eventId(EVIDENCE_UPLOAD_APPLICANT)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isNull();
                assertThat(result.caseData().getCcdState()).isEqualTo(CaseState.CASE_PROGRESSION);
            });
    }

    @Test
    void shouldAcceptRespondentEvidenceUploadWithoutChangingState() throws Exception {
        CaseData fixture = caseProgressionFixture();

        startWorkflow(fixture)
            .eventId(EVIDENCE_UPLOAD_RESPONDENT)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isNull();
                assertThat(result.caseData().getCcdState()).isEqualTo(CaseState.CASE_PROGRESSION);
            });
    }

    private static CaseData caseProgressionFixture() {
        return CaseDataTemplates.load("case-progression").toBuilder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .caseAccessCategory(UNSPEC_CLAIM)
            .build();
    }
}
