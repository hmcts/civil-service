package uk.gov.hmcts.reform.civil.callback;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum CallbackType {
    ABOUT_TO_START("about-to-start"),
    ABOUT_TO_SUBMIT("about-to-submit"),
    SUBMITTED("submitted"),
    MID("mid");

    private final String value;

    public static CallbackType fromValue(String value) {
        return Arrays.stream(values()).filter(event -> event.value.equals(value))
            .findFirst()
            .orElseThrow(() -> new CallbackException("Unknown Callback Type: " + value));
    }
}
