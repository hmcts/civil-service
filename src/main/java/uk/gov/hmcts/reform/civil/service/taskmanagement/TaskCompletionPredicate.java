package uk.gov.hmcts.reform.civil.service.taskmanagement;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.taskmanagement.Task;

import java.util.function.Predicate;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.model.taskmanagement.PermissionTypes.CLAIM;

public class TaskCompletionPredicate {

    private TaskCompletionPredicate() {
        //NO-OP
    }

    public static Predicate<Task> taskToCompleteFilter(CaseData caseData) {
        return task ->
            nonNull(task.getAdditionalProperties())
                && task.getPermissions().getValues().contains(CLAIM)
                && task.getAdditionalProperties().get("messageId").equals(caseData.getLastMessage().getMessageId());
    }

}
