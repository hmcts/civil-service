package uk.gov.hmcts.reform.civil.service.taskmanagement;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sendandreply.Message;
import uk.gov.hmcts.reform.civil.model.taskmanagement.Task;
import uk.gov.hmcts.reform.civil.model.taskmanagement.TaskPermissions;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.model.taskmanagement.PermissionTypes.CLAIM;

class TaskCompletionPredicateTest {

    private static final String MESSAGE_ID = "12345";

    @Test
    void shouldReturnTrueWhenAllConditionsAreMet() {
        CaseData caseData = CaseData.builder()
            .lastMessage(new Message().setMessageId(MESSAGE_ID))
            .build();
        Task task = new Task()
            .setPermissions(new TaskPermissions().setValues(Set.of(CLAIM)))
            .setAdditionalProperties(Map.of("messageId", MESSAGE_ID));
        Predicate<Task> predicate = TaskCompletionPredicate.taskToCompleteFilter(caseData);

        assertTrue(predicate.test(task));
    }

    @Test
    void shouldReturnFalseWhenAdditionalPropertiesAreNull() {
        CaseData caseData = CaseData.builder()
            .lastMessage(new Message().setMessageId(MESSAGE_ID))
            .build();
        Task task = new Task()
            .setPermissions(new TaskPermissions().setValues(Set.of(CLAIM)))
            .setAdditionalProperties(null);
        Predicate<Task> predicate = TaskCompletionPredicate.taskToCompleteFilter(caseData);

        assertFalse(predicate.test(task));
    }

    @Test
    void shouldReturnFalseWhenTaskTaskPermissionsDoNotContainClaim() {
        CaseData caseData = CaseData.builder()
            .lastMessage(new Message().setMessageId(MESSAGE_ID))
            .build();
        Task task = new Task()
            .setPermissions(new TaskPermissions().setValues(Collections.emptySet()))
            .setAdditionalProperties(Map.of("messageId", MESSAGE_ID));
        Predicate<Task> predicate = TaskCompletionPredicate.taskToCompleteFilter(caseData);

        assertFalse(predicate.test(task));
    }

    @Test
    void shouldReturnFalseWhenMessageIdsDoNotMatch() {
        CaseData caseData = CaseData.builder()
            .lastMessage(new Message().setMessageId(MESSAGE_ID))
            .build();
        Task task = new Task()
            .setPermissions(new TaskPermissions().setValues(Set.of(CLAIM)))
            .setAdditionalProperties(Map.of("messageId", "differentId"));
        Predicate<Task> predicate = TaskCompletionPredicate.taskToCompleteFilter(caseData);

        assertFalse(predicate.test(task));
    }

    @Test
    void shouldReturnFalseWhenMessageIdIsNull() {
        CaseData caseData = CaseData.builder()
            .lastMessage(new Message().setMessageId(null))
            .build();
        Task task = new Task()
            .setPermissions(new TaskPermissions().setValues(Set.of(CLAIM)))
            .setAdditionalProperties(Map.of("messageId", MESSAGE_ID));
        Predicate<Task> predicate = TaskCompletionPredicate.taskToCompleteFilter(caseData);

        assertFalse(predicate.test(task));
    }
}
