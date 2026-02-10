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
public class SearchParameterList implements SearchParameter<List<String>> {

    private SearchParameterKey key;
    private SearchOperator operator;

    @JsonProperty("values")
    private List<String> values;
}
