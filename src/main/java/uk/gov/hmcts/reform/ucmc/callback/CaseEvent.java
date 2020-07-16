package uk.gov.hmcts.reform.ucmc.callback;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CaseEvent {
    CREATE_CASE("CREATE_CLAIM"),
    CONFIRM_SERVICE("CONFIRM_SERVICE");

    private final String value;
}
