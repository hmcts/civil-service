package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.CreateLipClaimFixtures;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("java:S5960")
class CreateLipClaimWorkflowTest extends WorkflowIntegrationTest {

    @Test
    void shouldSetSpecCaseAccessCategoryAtAboutToStart() throws Exception {
        startWorkflow(CreateLipClaimFixtures.caseData())
            .eventId(CaseEvent.CREATE_LIP_CLAIM)
            .aboutToStart()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.caseData().getCaseAccessCategory()).isEqualTo(CaseCategory.SPEC_CLAIM);
            });
    }
}
