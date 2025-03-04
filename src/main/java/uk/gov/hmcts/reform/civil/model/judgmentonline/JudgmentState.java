package uk.gov.hmcts.reform.civil.model.judgmentonline;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JudgmentState {
    REQUESTED("requested"),
    ISSUED("issued"),
    MODIFIED("modified"),
    SATISFIED ("satisfied"),
    SET_ASIDE("set aside"),
    SET_ASIDE_ERROR("set aside in error"),
    CANCELLED("cancelled");

    private final String label;
}
