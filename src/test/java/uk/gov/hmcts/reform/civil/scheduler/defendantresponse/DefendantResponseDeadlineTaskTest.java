package uk.gov.hmcts.reform.civil.scheduler.defendantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefendantResponseDeadlineTaskTest {

    private static final long CASE_ID = 123L;

    @Mock
    private DefaultBackPressureConfiguration defaultBackPressureConfiguration;
    @Mock
    private ScheduledTaskBackPressureConfiguration backPressureConfiguration;

    private DefendantResponseDeadlineTask task;
    private CaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        task = new DefendantResponseDeadlineTask(defaultBackPressureConfiguration);
        caseDetails = CaseDetails.builder().id(CASE_ID).build();
    }

    @Test
    void shouldReturnCaseId() {
        assertThat(task.getItemId(caseDetails)).isEqualTo(CASE_ID);
    }

    @Test
    void shouldUseDefaultBackPressureConfiguration() {
        when(defaultBackPressureConfiguration.getDefaultBackPressure()).thenReturn(backPressureConfiguration);

        assertThat(task.backPressureConfiguration()).isSameAs(backPressureConfiguration);
    }

    @Test
    void shouldAcceptCaseDetails() {
        assertThatCode(() -> task.accept(caseDetails)).doesNotThrowAnyException();
    }
}
