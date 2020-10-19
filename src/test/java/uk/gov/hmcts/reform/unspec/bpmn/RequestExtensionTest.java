package uk.gov.hmcts.reform.unspec.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RequestExtensionTest extends BpmnBaseTest {

    public static final String TOPIC_NAME = "processCaseEvent";

    public RequestExtensionTest() {
        super("request_extension.bpmn", "REQUEST_EXTENSION_PROCESS_ID");
    }

    @Test
    void caseEventTaskShouldFireCaseEventExternalTask_whenStarted() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert topic names
        assertThat(getTopics()).containsOnly(TOPIC_NAME);

        //assert message start event
        assertThat(getProcessDefinitionByMessage("REQUEST_EXTENSION").getKey())
            .isEqualTo("REQUEST_EXTENSION_PROCESS_ID");

        //get external tasks
        List<ExternalTask> externalTasks = getExternalTasks();
        assertThat(externalTasks).hasSize(1);

        //fetch and complete task
        List<LockedExternalTask> lockedExternalTasks = fetchAndLockTask(TOPIC_NAME);

        assertThat(lockedExternalTasks).hasSize(1);
        assertThat(lockedExternalTasks.get(0).getVariables())
            .containsEntry("CASE_EVENT", "NOTIFY_APPLICANT_SOLICITOR1_FOR_REQUEST_FOR_EXTENSION");
        assertThat(lockedExternalTasks.get(0).getActivityId()).isEqualTo("RequestExtensionNotifyApplicantSolicitor1");

        completeTask(lockedExternalTasks.get(0).getId());

        //assert no external tasks left
        List<ExternalTask> externalTasksAfter = getExternalTasks();
        assertThat(externalTasksAfter).isEmpty();
    }
}
