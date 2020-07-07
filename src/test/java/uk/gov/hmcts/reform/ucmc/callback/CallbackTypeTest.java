package uk.gov.hmcts.reform.ucmc.callback;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CallbackTypeTest {

    @Test
    public void shouldDeserialiseWhenValidCallbacks() {
        assertThat(CallbackType.fromValue("mid")).isEqualTo(CallbackType.MID);
        assertThat(CallbackType.fromValue("about-to-start"))
            .isEqualTo(CallbackType.ABOUT_TO_START);
        assertThat(CallbackType.fromValue("about-to-submit"))
            .isEqualTo(CallbackType.ABOUT_TO_SUBMIT);
        assertThat(CallbackType.fromValue("submitted"))
            .isEqualTo(CallbackType.SUBMITTED);
    }

    @Test
    public void shouldThrowCallbackExceptionWhenUnknownCallback() {
        assertThrows(CallbackException.class, () -> CallbackType.fromValue("nope"));
    }
}
