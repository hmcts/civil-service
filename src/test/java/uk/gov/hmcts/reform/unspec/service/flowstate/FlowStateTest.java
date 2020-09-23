package uk.gov.hmcts.reform.unspec.service.flowstate;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FlowStateTest {

    @ParameterizedTest
    @EnumSource(value = FlowState.Main.class)
    void shouldReturnValidFlowState_whenMainFlowStateName(FlowState.Main flowState) {
        assertThat(FlowState.fromFullName(flowState.fullName()))
            .isEqualTo(flowState);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"INCORRECT.DRAFT", "MAIN.INCORRECT"})
    void shouldThrowIllegalArgumentException_whenInvalidName(String flowStateName) {
        assertThrows(
            IllegalArgumentException.class,
            () -> FlowState.fromFullName(flowStateName)
        );
    }
}
