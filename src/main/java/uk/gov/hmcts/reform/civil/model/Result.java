package uk.gov.hmcts.reform.civil.model;

public sealed interface Result {

    record Success() implements Result { }

    record Error(ExceptionRecord exceptionRecord) implements Result { }
}
