package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CoscApplicationStatus {
    ACTIVE("Active"),
    PROCESSED("Processed"),
    INACTIVE("Inactive");

    private final String status;
}
