package uk.gov.hmcts.reform.civil.model.wa;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
    name = "SortingParameter",
    description = "Sorting parameter containing the field to sort on and the order"
)
@EqualsAndHashCode
@ToString
public class SortingParameter {

    @Schema(requiredMode = REQUIRED,
        name = "sort_by",
        allowableValues = "dueDate, due_date, taskTitle, task_title, locationName, location_name, caseCategory, "
            + "case_category, caseId, case_id, caseName, case_name, nextHearingDate, next_hearing_date",
        description = "Support snake_case and camelCase values",
        example = "due_date")
    private final SortField sortBy;
    @Schema(
        requiredMode = REQUIRED,
        name = "sort_order",
        allowableValues = "asc, desc",
        example = "asc")
    private final SortOrder sortOrder;

    public SortingParameter(SortField sortBy, SortOrder sortOrder) {
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
    }

    public SortField getSortBy() {
        return sortBy;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }
}
