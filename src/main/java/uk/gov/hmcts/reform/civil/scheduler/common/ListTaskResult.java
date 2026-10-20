package uk.gov.hmcts.reform.civil.scheduler.common;

import java.util.List;
import java.util.stream.Stream;

public record ListTaskResult<T>(List<T> items) implements TaskResult<T> {

    @Override
    public int totalResults() {
        return items.size();
    }

    @Override
    public Stream<T> itemStream() {
        return items.stream();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }
}
