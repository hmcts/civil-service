package uk.gov.hmcts.reform.civil.model.wa;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
    name = "SearchParameterList",
    description = "Search parameter containing the key, operator and a list of values"
)
@EqualsAndHashCode
@ToString
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class SearchParameterList implements SearchParameter<List<String>> {

    @Schema(
        requiredMode = REQUIRED,
        allowableValues = "location, user, jurisdiction, state, taskId, taskType, caseId, work_type, role_category",
        example = "user")
    @NotNull(
        message = "Each search_parameter element must have 'key', 'values' and 'operator' fields present and populated."
    )
    private final SearchParameterKey key;

    @Schema(allowableValues = "IN", example = "IN")
    private final SearchOperator operator;

    @Schema(
        requiredMode = REQUIRED,
        example = "[\"998db99b-08aa-43d4-bc6b-0aabbb0e3c6f\"]", nullable = true)
    @NotNull(
        message = "Each search_parameter element must have 'key', 'values' and 'operator' fields present and populated."
    )
    @NotEmpty(
        message = "Each search_parameter element must have 'key', 'values' and 'operator' fields present and populated."
    )
    private final List<String> values;

    @JsonCreator
    public SearchParameterList(SearchParameterKey key, SearchOperator operator, List<String> values) {
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
    public List<String> getValues() {
        return values;
    }
}
