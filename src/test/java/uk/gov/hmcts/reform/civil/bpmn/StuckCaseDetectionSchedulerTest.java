package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.springframework.scheduling.support.CronExpression;
import org.camunda.bpm.engine.management.JobDefinition;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class StuckCaseDetectionSchedulerTest extends BpmnBaseTest {

    private static final String TOPIC_NAME = "STUCK_CASE_DETECTION_EVENT";
    private static final String CRON = "0 15 * * * ?";

    StuckCaseDetectionSchedulerTest() {
        super("stuck_case_detection_scheduler.bpmn", "STUCK_CASE_DETECTION_SCHEDULER");
    }

    @Test
    void schedulerShouldRaiseStuckCaseDetectionExternalTask_whenStarted() {
        assertFalse(processInstance.isEnded());

        assertThat(getTopics()).containsOnly(TOPIC_NAME);

        List<JobDefinition> jobDefinitions = getJobs();
        assertThat(jobDefinitions).hasSize(1);
        assertThat(jobDefinitions.get(0).getJobType()).isEqualTo("timer-start-event");
        assertThat(jobDefinitions.get(0).getJobConfiguration()).isEqualTo("CYCLE: " + CRON);

        assertCronTriggerFiresAtExpectedTime(
            CronExpression.parse(CRON),
            LocalDateTime.of(2024, 11, 30, 0, 0),
            LocalDateTime.of(2024, 11, 30, 0, 15)
        );

        List<ExternalTask> externalTasks = getExternalTasks();
        assertThat(externalTasks).hasSize(1);

        List<LockedExternalTask> lockedExternalTasks = fetchAndLockTask(TOPIC_NAME);
        assertThat(lockedExternalTasks).hasSize(1);
        completeTask(lockedExternalTasks.get(0).getId());

        assertThat(getExternalTasks()).isEmpty();
        assertFalse(processInstance.isEnded());
    }
}
