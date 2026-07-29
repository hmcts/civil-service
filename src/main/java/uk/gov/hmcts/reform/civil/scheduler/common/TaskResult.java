package uk.gov.hmcts.reform.civil.scheduler.common;

import java.util.stream.Stream;

public interface TaskResult<T> {

    int totalResults();

    Stream<T> itemStream();

    boolean isEmpty();
}
