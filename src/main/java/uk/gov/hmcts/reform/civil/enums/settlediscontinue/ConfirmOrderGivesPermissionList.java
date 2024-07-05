package uk.gov.hmcts.reform.civil.enums.settlediscontinue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConfirmOrderGivesPermissionList {
    YES("Yes"),
    NO("No");

    private final String displayedValue;
}
