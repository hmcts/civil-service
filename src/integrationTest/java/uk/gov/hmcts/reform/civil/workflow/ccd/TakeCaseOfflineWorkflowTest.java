package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.TakeCaseOfflineFixtures;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SuppressWarnings("java:S5960")
class TakeCaseOfflineWorkflowTest extends WorkflowIntegrationTest {

    private static final LocalDateTime TAKEN_OFFLINE_AT = LocalDateTime.of(2026, 5, 19, 13, 0);

    @MockBean
    private Time time;

    @Test
    void shouldSetTakenOfflineDateAndBusinessProcessAtAboutToSubmit() throws Exception {
        when(time.now()).thenReturn(TAKEN_OFFLINE_AT);

        startWorkflow(TakeCaseOfflineFixtures.lipCaseData())
            .eventId(CaseEvent.TAKE_CASE_OFFLINE)
            .caseDataBefore(TakeCaseOfflineFixtures.lipCaseDataBefore())
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.caseData().getTakenOfflineDate()).isEqualTo(TAKEN_OFFLINE_AT);
                assertThat(result.caseData().getPreviousCCDState()).isEqualTo(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);
                assertThat(result.caseData().getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(BusinessProcessStatus.READY, CaseEvent.TAKE_CASE_OFFLINE.name());
            });
    }
}
