package uk.gov.hmcts.reform.civil.model.taskmanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class SortingParameter {

    @JsonProperty("sort_by")
    private SortField sortBy;

    @JsonProperty("sort_order")
    private SortOrder sortOrder;
}
