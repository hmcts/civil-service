package uk.gov.hmcts.reform.civil.scheduler.common;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SetTaskResultTest {

    @Test
    void shouldExposeItemsAndTotalResults() {
        SetTaskResult<String> result = new SetTaskResult<>(Set.of("one", "two"));

        assertThat(result.totalResults()).isEqualTo(2);
        assertThat(result.itemStream()).containsExactlyInAnyOrder("one", "two");
        assertThat(result.isEmpty()).isFalse();
    }

    @Test
    void shouldBeEmptyWhenSetIsEmpty() {
        SetTaskResult<String> result = new SetTaskResult<>(Set.of());

        assertThat(result.isEmpty()).isTrue();
    }
}
