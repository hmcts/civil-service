package uk.gov.hmcts.reform.civil.workflow.ga;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.service.GenAppStateHelperService;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SuppressWarnings("java:S5960")
class TriggerMoveApplicationOfflineWorkflowTest extends WorkflowIntegrationTest {

    private static final long CASE_ID = 1234567890123456L;

    @MockBean
    private GenAppStateHelperService helperService;

    @Test
    void shouldTriggerTheOfflineEventWhenTheMainCaseHasApplications() throws Exception {
        CaseData caseData = caseDataWithApplication();

        startWorkflow(caseData)
            .eventId(CaseEvent.TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE)
            .aboutToSubmit()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty());

        verify(helperService).triggerEvent(
            argThat(actual -> actual.getCcdCaseReference().equals(CASE_ID)
                && actual.getGeneralApplications() != null
                && actual.getGeneralApplications().size() == 1),
            eq(CaseEvent.APPLICATION_PROCEEDS_IN_HERITAGE)
        );
    }

    @Test
    void shouldNotCallTheGaServiceWhenTheMainCaseHasNoApplications() throws Exception {
        CaseData caseData = CaseData.builder().ccdCaseReference(CASE_ID).build();

        startWorkflow(caseData)
            .eventId(CaseEvent.TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE)
            .aboutToSubmit()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty());

        verifyNoInteractions(helperService);
    }

    @Test
    void shouldReturnAnErrorWhenTheOfflineEventCannotBeTriggered() throws Exception {
        CaseData caseData = caseDataWithApplication();
        doThrow(new IllegalStateException("CCD unavailable"))
            .when(helperService).triggerEvent(any(CaseData.class), eq(CaseEvent.APPLICATION_PROCEEDS_IN_HERITAGE));

        startWorkflow(caseData)
            .eventId(CaseEvent.TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE)
            .aboutToSubmit()
            .then(result -> assertThat(result.response().getErrors())
                .containsExactly("Could not trigger event to take application offline under the case: " + CASE_ID));
    }

    private CaseData caseDataWithApplication() {
        return CaseData.builder()
            .ccdCaseReference(CASE_ID)
            .generalApplications(wrapElements(new GeneralApplication()))
            .build();
    }
}
