package uk.gov.hmcts.reform.civil.model.taskmanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
public class SearchTaskRequest {

    @JsonProperty("search_parameters")
    private final List<SearchParameter<?>> searchParameters;

    @JsonProperty("sorting_parameters")
    private final List<SortingParameter> sortingParameters;

    @JsonProperty("request_context")
    private final RequestContext requestContext;
}
