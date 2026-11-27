package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.management.JobDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class OpenIncidentRetryHourlySchedulerTest extends BpmnBaseTest {

    private static final String TOPIC_NAME = "INCIDENT_RETRY_EVENT";
    private static final String CRON = "0 0 6-21 * * ?";

    OpenIncidentRetryHourlySchedulerTest() {
        super("open_incident_retry_hourly_scheduler.bpmn", "INCIDENT_RETRY_HOURLY_SCHEDULER");
    }

    @Test
    void schedulerShouldRaiseGuardedOnlyIncidentRetryTask_whenStarted() {
        assertFalse(processInstance.isEnded());

        assertThat(getTopics()).containsOnly(TOPIC_NAME);

        List<JobDefinition> jobDefinitions = getJobs();
        assertThat(jobDefinitions).hasSize(1);
        assertThat(jobDefinitions.get(0).getJobType()).isEqualTo("timer-start-event");
        assertThat(jobDefinitions.get(0).getJobConfiguration()).isEqualTo("CYCLE: " + CRON);

        CronExpression expression = CronExpression.parse(CRON);
        assertCronTriggerFiresAtExpectedTime(
            expression, LocalDateTime.of(2024, 11, 30, 0, 0), LocalDateTime.of(2024, 11, 30, 6, 0));
        assertCronTriggerFiresAtExpectedTime(
            expression, LocalDateTime.of(2024, 11, 30, 6, 0), LocalDateTime.of(2024, 11, 30, 7, 0));
        assertCronTriggerFiresAtExpectedTime(
            expression, LocalDateTime.of(2024, 11, 30, 20, 30), LocalDateTime.of(2024, 11, 30, 21, 0));
        assertCronTriggerFiresAtExpectedTime(
            expression, LocalDateTime.of(2024, 11, 30, 21, 0), LocalDateTime.of(2024, 12, 1, 6, 0));

        List<ExternalTask> externalTasks = getExternalTasks();
        assertThat(externalTasks).hasSize(1);

        List<LockedExternalTask> lockedExternalTasks = fetchAndLockTask(TOPIC_NAME);
        assertThat(lockedExternalTasks).hasSize(1);
        assertThat(lockedExternalTasks.get(0).getVariables())
            .containsEntry("retryMode", "GUARDED_ONLY")
            .containsEntry("incidentLookbackMinutes", "70");
        completeTask(lockedExternalTasks.get(0).getId());

        assertThat(getExternalTasks()).isEmpty();
        assertFalse(processInstance.isEnded());
    }
}
