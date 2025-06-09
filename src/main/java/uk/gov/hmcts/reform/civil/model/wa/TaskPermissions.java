package uk.gov.hmcts.reform.civil.model.wa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(allowableValues = "Permissions")
@ToString
@EqualsAndHashCode
public class TaskPermissions {

    @Schema(
        description = "A Union of all permissions held for a task.")
    Set<PermissionTypes> values;

    public TaskPermissions(Set<PermissionTypes> values) {
        this.values = values;
    }

    public Set<PermissionTypes> getValues() {
        return values;
    }
}
