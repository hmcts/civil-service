package uk.gov.hmcts.reform.unspec.stateflow.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StateTest {

    @Test
    void shouldCreateStateFrom() {
        assertThat(State.from("STATE"))
            .extracting(State::getName)
            .isNotNull()
            .isEqualTo("STATE");
    }

    @Test
    void shouldCreateErrorState() {
        assertThat(State.error())
            .extracting(State::getName)
            .isNotNull()
            .isEqualTo(State.ERROR_STATE);
    }

}
