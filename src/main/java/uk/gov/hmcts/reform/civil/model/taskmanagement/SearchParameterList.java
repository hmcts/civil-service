package uk.gov.hmcts.reform.civil.model.taskmanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
public class SearchParameterList implements SearchParameter<List<String>> {

    private final SearchParameterKey key;
    private final SearchOperator operator;

    @JsonProperty("values")
    private final List<String> values;
}
