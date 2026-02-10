package uk.gov.hmcts.reform.civil.model.taskmanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SearchTaskRequest {

    @JsonProperty("search_parameters")
    private List<SearchParameter<?>> searchParameters;

    @JsonProperty("sorting_parameters")
    private List<SortingParameter> sortingParameters;

    @JsonProperty("request_context")
    private RequestContext requestContext;
}
