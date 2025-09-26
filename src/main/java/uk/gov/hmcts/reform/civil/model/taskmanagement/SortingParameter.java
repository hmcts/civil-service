package uk.gov.hmcts.reform.civil.model.taskmanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
@Builder
public class SortingParameter {

    @JsonProperty("sort_by")
    private final SortField sortBy;

    @JsonProperty("sort_order")
    private final SortOrder sortOrder;
}
