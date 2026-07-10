package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TriggerSchedulerTest extends BpmnBaseTest {

    public static final String TOPIC_NAME = "TRIGGER_SCHEDULER";
    public static final String ACTIVITY_ID = "TriggerScheduler";

    public TriggerSchedulerTest() {
        super("trigger_scheduler.bpmn", "TRIGGER_SCHEDULER");
    }

    @Test
    void shouldSendTriggerSchedulerEventWithSchedulerNameVariable() {
        // Given
        VariableMap variables = Variables.createVariables();
        variables.put("schedulerName", "TestScheduler");

        // Set variables on the process instance started in setup()
        engine.getRuntimeService().setVariables(processInstance.getId(), variables);

        // When
        // Get the external task and assert it has the correct topic
        ExternalTask externalTask = assertNextExternalTask(TOPIC_NAME);
        assertThat(externalTask.getActivityId()).isEqualTo(ACTIVITY_ID);

        // fetch and complete task
        List<LockedExternalTask> lockedExternalTasks = fetchAndLockTask(TOPIC_NAME);
        assertThat(lockedExternalTasks).hasSize(1);
        assertThat(lockedExternalTasks.get(0).getVariables()).containsEntry("schedulerName", "TestScheduler");

        completeTask(lockedExternalTasks.get(0).getId());

        // Then
        // Assert no external tasks left and process is ended
        assertNoExternalTasksLeft();
        assertThat(engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0);
    }
}
