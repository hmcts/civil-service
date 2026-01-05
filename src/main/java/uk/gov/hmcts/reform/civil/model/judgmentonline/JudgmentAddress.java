package uk.gov.hmcts.reform.civil.model.judgmentonline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgmentAddress {

    private String defendantAddressLine1;
    private String defendantAddressLine2;
    private String defendantAddressLine3;
    private String defendantAddressLine4;
    private String defendantAddressLine5;
    private String defendantPostCode;
}
