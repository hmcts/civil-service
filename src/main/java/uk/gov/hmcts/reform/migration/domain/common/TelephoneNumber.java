package uk.gov.hmcts.reform.migration.domain.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TelephoneNumber {
    private final String phoneNumber;
    private final String phoneUsageType;
    private final String contactDirection;
}
