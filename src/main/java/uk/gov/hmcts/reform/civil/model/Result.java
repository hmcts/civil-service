package uk.gov.hmcts.reform.civil.model;

import java.util.Map;

public sealed interface Result {

    record Success() implements Result { }

    record Error(String eventName, Map<String, String> messageProps) implements Result { }
}
