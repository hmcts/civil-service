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
public class SearchParameterBoolean implements SearchParameter<Boolean> {

    private final SearchParameterKey key;
    private final SearchOperator operator;

    @JsonProperty("value")
    private final Boolean values;
}
