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
            .lastMessage(Message.builder().messageId(MESSAGE_ID).build())
            .build();
        Task task = Task.builder()
            .permissions(TaskPermissions.builder().values(Set.of(CLAIM)).build())
            .additionalProperties(Map.of("messageId", MESSAGE_ID))
            .build();
        Predicate<Task> predicate = TaskCompletionPredicate.taskToCompleteFilter(caseData);

        assertTrue(predicate.test(task));
    }

    @Test
    void shouldReturnFalseWhenAdditionalPropertiesAreNull() {
        CaseData caseData = CaseData.builder()
            .lastMessage(Message.builder().build().builder().messageId(MESSAGE_ID).build())
            .build();
        Task task = Task.builder()
            .permissions(TaskPermissions.builder().values(Set.of(CLAIM)).build())
            .additionalProperties(null)
            .build();
        Predicate<Task> predicate = TaskCompletionPredicate.taskToCompleteFilter(caseData);

        assertFalse(predicate.test(task));
    }

    @Test
    void shouldReturnFalseWhenTaskTaskPermissionsDoNotContainClaim() {
        CaseData caseData = CaseData.builder()
            .lastMessage(Message.builder().messageId(MESSAGE_ID).build())
            .build();
        Task task = Task.builder()
            .permissions(TaskPermissions.builder().values(Collections.emptySet()).build())
            .additionalProperties(Map.of("messageId", MESSAGE_ID))
            .build();
        Predicate<Task> predicate = TaskCompletionPredicate.taskToCompleteFilter(caseData);

        assertFalse(predicate.test(task));
    }

    @Test
    void shouldReturnFalseWhenMessageIdsDoNotMatch() {
        CaseData caseData = CaseData.builder()
            .lastMessage(Message.builder().messageId(MESSAGE_ID).build())
            .build();
        Task task = Task.builder()
            .permissions(TaskPermissions.builder().values(Set.of(CLAIM)).build())
            .additionalProperties(Map.of("messageId", "differentId"))
            .build();
        Predicate<Task> predicate = TaskCompletionPredicate.taskToCompleteFilter(caseData);

        assertFalse(predicate.test(task));
    }

    @Test
    void shouldReturnFalseWhenMessageIdIsNull() {
        CaseData caseData = CaseData.builder()
            .lastMessage(Message.builder().messageId(null).build())
            .build();
        Task task = Task.builder()
            .permissions(TaskPermissions.builder().values(Set.of(CLAIM)).build())
            .additionalProperties(Map.of("messageId", MESSAGE_ID))
            .build();
        Predicate<Task> predicate = TaskCompletionPredicate.taskToCompleteFilter(caseData);

        assertFalse(predicate.test(task));
    }
}
