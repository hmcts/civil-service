package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.RequestJudgementByAdmissionFixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REQUEST_JUDGEMENT_ADMISSION_SPEC;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;

@SuppressWarnings({"java:S5960", "java:S6813"})
class RequestJudgementByAdmissionWorkflowTest extends WorkflowIntegrationTest {

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    void shouldSubmitJBAForOneVOnePayImmediately() throws Exception {
        CaseData fixture = RequestJudgementByAdmissionFixtures.jba1v1PayImmediately();

        startWorkflow(fixture)
            .eventId(REQUEST_JUDGEMENT_ADMISSION_SPEC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState())
                    .isEqualTo(CaseState.All_FINAL_ORDERS_ISSUED.name());

                CaseData updated = result.caseData();
                assertThat(updated.getActiveJudgment()).isNotNull();
                assertThat(updated.getJoIsLiveJudgmentExists()).isEqualTo(YesOrNo.YES);
                assertThat(updated.getJoJudgementByAdmissionIssueDate()).isNotNull();

                assertThat(updated.getBusinessProcess().getStatus()).isEqualTo(READY);
                assertThat(updated.getBusinessProcess().getCamundaEvent())
                    .isEqualTo(JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC.name());
            })
            .submitted()
            .then(result -> assertThat(result.submittedResponse()).isNotNull());
    }

    @Test
    void shouldReturnErrorWhenNotEligibleForJBA() throws Exception {
        CaseData fixture = RequestJudgementByAdmissionFixtures.jbaNotEligibleDateNotPermitted();

        startWorkflow(fixture)
            .eventId(REQUEST_JUDGEMENT_ADMISSION_SPEC)
            .aboutToStart()
            .then(result -> {
                assertThat(result.response().getErrors()).isNotEmpty();
                assertThat(result.response().getErrors().get(0))
                    .contains("The Claim is not eligible for Request Judgment By Admission until");
            });
    }
}
