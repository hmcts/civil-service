package uk.gov.hmcts.reform.unspec.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CaseEventTaskTest extends BpmnBaseTest {

    public static final String TOPIC_NAME = "processCaseEvent";

    public CaseEventTaskTest() {
        super("claim_issue.bpmn", "Claim_issue_event_handling");
    }

    @Test
    void caseEventTaskShouldFireCaseEventExternalTask_whenStarted() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert topic names
        assertThat(getTopics()).containsOnly(TOPIC_NAME);

        //get external tasks
        List<ExternalTask> externalTasks = getExternalTasks();

        //assert task is as expected
        assertThat(externalTasks).hasSize(1);
        assertThat(externalTasks.get(0).getTopicName()).isEqualTo("processCaseEvent");

        //fetch and complete task
        List<LockedExternalTask> lockedExternalTasks = fetchAndLockTask(TOPIC_NAME);

        assertThat(lockedExternalTasks).hasSize(1);
        assertThat(lockedExternalTasks.get(0).getVariables())
            .containsEntry("CCD_ID", "1600774271669709")
            .containsEntry("CASE_EVENT", "NOTIFY_DEFENDANT_SOLICITOR_FOR_CLAIM_ISSUE")
        ;

        completeTask(lockedExternalTasks.get(0).getId());

        //assert no external tasks left
        List<ExternalTask> externalTasksAfter = getExternalTasks();
        assertThat(externalTasksAfter).isEmpty();
    }
}
