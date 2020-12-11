package uk.gov.hmcts.reform.unspec.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupportRequirements {
    DISABLED_ACCESS("Disabled access"),
    HEARING_LOOPS("Hearing loop"),
    SIGN_INTERPRETER("Sign language interpreter"),
    LANGUAGE_INTERPRETER("Language interpreter"),
    OTHER_SUPPORT("Other support");

    private final String displayedValue;
}
