package uk.gov.hmcts.reform.civil.scheduler.common;

import java.util.List;
import java.util.stream.Stream;

public record ListTaskResult<T>(List<T> items, int totalResults) implements TaskResult<T> {

    @Override
    public Stream<T> itemStream() {
        return items.stream();
    }

    @Override
    public boolean isEmpty() {
        return totalResults == 0;
    }
}
