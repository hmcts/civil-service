package uk.gov.hmcts.reform.civil.controllers.dashboard.util;

import uk.gov.hmcts.reform.dashboard.data.TaskList;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class Evaluations {

    private Evaluations() {
        // throw an AssertionError if the constructor is accidentally invoked from within the class
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static void evaluateMediationTasklist(List<TaskList> response, List<TaskList> taskListExpected) {
        assertThat(response).usingRecursiveFieldByFieldElementComparatorIgnoringFields(
            "id",
            "createdAt",
            "updatedAt",
            "messageParams"
        ).isEqualTo(
            taskListExpected);
    }

    public static void evaluateSizeOfTasklist(int response, int taskListExpected) {
        assertThat(response).isEqualTo(taskListExpected);
    }
}
