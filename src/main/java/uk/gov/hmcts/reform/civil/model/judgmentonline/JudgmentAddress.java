package uk.gov.hmcts.reform.civil.model.judgmentonline;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JudgmentAddress {

    private String defendantAddressLine1;
    private String defendantAddressLine2;
    private String defendantAddressLine3;
    private String defendantAddressLine4;
    private String defendantAddressLine5;
    private String defendantPostCode;
}
