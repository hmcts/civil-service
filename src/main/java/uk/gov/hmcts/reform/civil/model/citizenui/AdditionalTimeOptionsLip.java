package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdditionalTimeOptionsLip {
    MORE_THAN_28_DAYS("more-than-28-days"),
    UP_TO_28_DAYS("up-to-28-days");
    private final String value;
}
