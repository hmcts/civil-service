package uk.gov.hmcts.reform.civil.model.taskmanagement;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class TaskPermissions {

    Set<PermissionTypes> values;

    public Set<PermissionTypes> getValues() {
        return values;
    }
}
