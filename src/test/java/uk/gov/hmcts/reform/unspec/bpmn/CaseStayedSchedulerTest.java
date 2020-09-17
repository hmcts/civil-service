package uk.gov.hmcts.reform.unspec.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.management.JobDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CaseStayedSchedulerTest extends BpmnBaseTest {

    public static final String TOPIC_NAME = "CASE_STAYED_FINDER";

    public CaseStayedSchedulerTest() {
        super("case_stayed_scheduler.bpmn", "Process_05o55pg");
    }

    @Test
    void caseStayedSchedulerBmpnShouldFireCaseStayedExternalTask_whenStarted() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert topic names
        assertThat(getTopics()).containsOnly(TOPIC_NAME);

        //get jobs
        List<JobDefinition> jobDefinitions = getJobs();

        //assert that job is as expected
        assertThat(jobDefinitions).hasSize(1);
        assertThat(jobDefinitions.get(0).getJobType()).isEqualTo("timer-start-event");

        //TODO update CRON schedule.
        assertThat(jobDefinitions.get(0).getJobConfiguration()).isEqualTo("CYCLE: 0 0/5 * * * ?");

        //get external tasks
        List<ExternalTask> externalTasks = getExternalTasks();

        //assert task is as expected
        assertThat(externalTasks).hasSize(1);
        assertThat(externalTasks.get(0).getTopicName()).isEqualTo("CASE_STAYED_FINDER");

        //fetch and complete task
        List<LockedExternalTask> lockedExternalTasks = fetchAndLockTask(TOPIC_NAME);

        assertThat(lockedExternalTasks).hasSize(1);
        completeTask(lockedExternalTasks.get(0).getId());

        //assert no external tasks left
        List<ExternalTask> externalTasksAfter = getExternalTasks();
        assertThat(externalTasksAfter).isEmpty();

        //assert process is still active - timer event so always running
        assertFalse(processInstance.isEnded());
    }
}
