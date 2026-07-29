package uk.gov.hmcts.reform.civil.scheduler.defendantresponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefendantResponseDeadlineTaskTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    @Mock
    private ScheduledTaskBackPressureConfiguration backPressureConfiguration;

    @InjectMocks
    private DefendantResponseDeadlineTask task;

    @Test
    void shouldTriggerDefendantResponseDeadlineCheckEvent_whenAcceptIsCalled() {
        Long caseId = 123L;
        CaseDetails caseDetails = CaseDetails.builder().id(caseId).build();

        task.accept(caseDetails);

        verify(coreCaseDataService).triggerEvent(caseId, CaseEvent.DEFENDANT_RESPONSE_DEADLINE_CHECK);
    }

    @Test
    void shouldUseDefaultBackPressureConfiguration() {
        when(defaultBackPressureConfiguration.getDefaultBackPressure()).thenReturn(backPressureConfiguration);

        assertThat(task.backPressureConfiguration()).isSameAs(backPressureConfiguration);
    }
}
