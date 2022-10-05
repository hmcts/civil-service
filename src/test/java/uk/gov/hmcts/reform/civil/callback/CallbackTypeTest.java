package uk.gov.hmcts.reform.civil.callback;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CallbackTypeTest {

    @ParameterizedTest
    @EnumSource(value = CallbackType.class)
    void shouldDeserialize_whenValidCallbacks(CallbackType callbackType) {
        assertThat(CallbackType.fromValue(callbackType.getValue())).isEqualTo(callbackType);
    }

    @Test
    void shouldThrowCallbackException_whenUnknownCallback() {
        assertThrows(CallbackException.class, () -> CallbackType.fromValue("nope"));
    }
}
