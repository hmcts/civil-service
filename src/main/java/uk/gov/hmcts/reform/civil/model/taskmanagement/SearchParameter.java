package uk.gov.hmcts.reform.civil.model.taskmanagement;

public interface SearchParameter<T> {

    SearchParameterKey getKey();

    SearchOperator getOperator();

    T getValues();
}
