package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseOptionsLiP {
    ALREADY_AGREED("already-agreed"),
    NO("no"),
    REQUEST_REFUSED("request-refused"),
    YES("yes");

    private final String value;
}
