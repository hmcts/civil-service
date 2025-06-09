package uk.gov.hmcts.reform.civil.model.wa;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.launchdarkly.shaded.org.jetbrains.annotations.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
    name = "SearchParameterBoolean",
    description = "Search parameter containing the key, operator and boolean values"
)
@EqualsAndHashCode
@ToString
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class SearchParameterBoolean implements SearchParameter<Boolean> {

    @Schema(requiredMode = REQUIRED)
    @NotNull()
    private final SearchParameterKey key;

    @Schema(allowableValues = "BOOLEAN", example = "BOOLEAN")
    @NotNull()
    private final SearchOperator operator;

    @Schema(requiredMode = REQUIRED, example = "true")
    @NotNull()
    private final boolean values;

    @JsonCreator
    public SearchParameterBoolean(SearchParameterKey key, SearchOperator operator, boolean values) {
        this.key = key;
        this.operator = operator;
        this.values = values;
    }

    @Override
    public SearchParameterKey getKey() {
        return key;
    }

    @Override
    public SearchOperator getOperator() {
        return operator;
    }

    @Override
    @JsonProperty("value")
    public Boolean getValues() {
        return values;
    }
}
