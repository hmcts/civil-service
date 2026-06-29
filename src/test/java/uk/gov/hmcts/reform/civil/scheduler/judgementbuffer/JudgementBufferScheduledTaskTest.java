package uk.gov.hmcts.reform.civil.scheduler.judgementbuffer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JudgementBufferScheduledTaskTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private JudgementBufferScheduledTask task;

    @Test
    void shouldTriggerUpdateWhenCaseIsEligibleForDefaultJudgement() {
        Long caseId = 123L;
        CaseDetails caseDetails = CaseDetails.builder().id(caseId).build();

        task.accept(caseDetails);

        verify(coreCaseDataService).triggerEvent(caseId, CaseEvent.DEFAULT_JUDGEMENT_GRANTED_SPEC);
    }

    @Test
    void shouldReturnBackPressureConfigurationFromProperties() {
        ScheduledTaskBackPressureConfiguration backPressure = new ScheduledTaskBackPressureConfiguration(
            Duration.ofSeconds(1),
            Duration.ofSeconds(20),
            Duration.ofMillis(1000),
            Duration.ofMillis(500),
            Duration.ofMillis(200),
            Duration.ofSeconds(5)
        );

        DefaultBackPressureConfiguration configuration = new DefaultBackPressureConfiguration();
        configuration.setDefaultBackPressure(backPressure);

        ScheduledTaskBackPressureConfiguration result = task.backPressureConfiguration();

        assertThat(result).isEqualTo(backPressure);
    }
}
