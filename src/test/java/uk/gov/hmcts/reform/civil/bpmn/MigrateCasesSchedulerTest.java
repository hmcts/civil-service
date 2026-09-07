package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.management.JobDefinition;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MigrateCasesSchedulerTest extends BpmnBaseTest {

    private static final String TOPIC_NAME = "MIGRATE_CASES_EVENTS";
    private static final String TIMER_CYCLE = "R12/2080-01-01T00:00:00Z/P1M";

    MigrateCasesSchedulerTest() {
        super("migrate_cases_scheduler.bpmn", "MIGRATE_CASES_SCHEDULER");
    }

    @Test
    void schedulerShouldTriggerMigrateCasesJob_whenStarted() throws ParseException {
        assertFalse(processInstance.isEnded());

        assertThat(getTopics()).containsOnly(TOPIC_NAME);

        List<JobDefinition> jobDefinitions = getJobs();
        assertThat(jobDefinitions).hasSize(1);
        assertThat(jobDefinitions.get(0).getJobType()).isEqualTo("timer-start-event");
        assertThat(jobDefinitions.get(0).getJobConfiguration()).isEqualTo("CYCLE: " + TIMER_CYCLE);

        List<ExternalTask> externalTasks = getExternalTasks();
        assertThat(externalTasks).hasSize(1);

        List<LockedExternalTask> lockedExternalTasks = fetchAndLockTask(TOPIC_NAME);
        assertThat(lockedExternalTasks).hasSize(1);
        completeTask(lockedExternalTasks.get(0).getId());

        assertThat(getExternalTasks()).isEmpty();
        assertFalse(processInstance.isEnded());
    }
}
