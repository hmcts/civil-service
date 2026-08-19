package uk.gov.hmcts.reform.civil.scheduler.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ListTaskResultTest {

    @Test
    void shouldExposeItemsAndTotalResults() {
        ListTaskResult<String> result = new ListTaskResult<>(List.of("one", "two"), 2);

        assertThat(result.totalResults()).isEqualTo(2);
        assertThat(result.itemStream()).containsExactly("one", "two");
        assertThat(result.isEmpty()).isFalse();
    }

    @Test
    void shouldBeEmptyWhenTotalResultsIsZero() {
        ListTaskResult<String> result = new ListTaskResult<>(List.of("not-counted"), 0);

        assertThat(result.isEmpty()).isTrue();
    }
}
