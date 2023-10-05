package uk.gov.hmcts.reform.migration.domain.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class EmailAddress {
    private final String email;
    private final String emailUsageType;
}
