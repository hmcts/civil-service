package uk.gov.hmcts.reform.civil.model.wa;

public interface SearchParameter<T> {

    SearchParameterKey getKey();

    SearchOperator getOperator();

    T getValues();
}
