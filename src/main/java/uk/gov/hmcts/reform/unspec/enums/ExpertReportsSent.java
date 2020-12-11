package uk.gov.hmcts.reform.unspec.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExpertReportsSent {
    YES("Yes"),
    NO("No"),
    NOT_OBTAINED("Not yet obtained");

    private final String displayedValue;
}
