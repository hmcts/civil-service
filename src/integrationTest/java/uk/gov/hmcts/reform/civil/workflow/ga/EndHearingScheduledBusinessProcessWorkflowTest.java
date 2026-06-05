package uk.gov.hmcts.reform.civil.workflow.ga;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.service.ParentCaseUpdateHelper;
import uk.gov.hmcts.reform.civil.workflow.ga.fixture.EndHearingScheduledBusinessProcessFixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SuppressWarnings("java:S5960")
class EndHearingScheduledBusinessProcessWorkflowTest extends GAWorkflowIntegrationTest {

    @MockBean
    private ParentCaseUpdateHelper parentCaseUpdateHelper;

    @Test
    void shouldEndHearingScheduledBusinessProcessAtAboutToSubmit() throws Exception {
        var caseData = EndHearingScheduledBusinessProcessFixtures.caseData();

        startWorkflow(caseData)
            .eventId(CaseEvent.END_HEARING_SCHEDULED_PROCESS_GASPEC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.caseData().getCcdState()).isEqualTo(CaseState.HEARING_SCHEDULED);
            });

        verify(parentCaseUpdateHelper).updateParentWithGAState(any(), eq(CaseState.HEARING_SCHEDULED.getDisplayedValue()));
    }
}
