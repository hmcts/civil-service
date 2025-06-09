package uk.gov.hmcts.reform.civil.model.wa;

import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "SearchParameters",
    discriminatorProperty = "values",
    discriminatorMapping = {
        @DiscriminatorMapping(value = "SearchParameterBoolean", schema = SearchParameterBoolean.class),
        @DiscriminatorMapping(value = "SearchParameterList", schema = SearchParameterList.class)
    }
)
public interface SearchParameter<T> {

    SearchParameterKey getKey();

    SearchOperator getOperator();

    T getValues();
}
