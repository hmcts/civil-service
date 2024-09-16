package uk.gov.hmcts.reform.civil.model.judgmentonline.cjes;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JudgementAddress {

    private String defendantAddressLine1;
    private String defendantAddressLine2;
    private String defendantAddressLine3;
    private String defendantAddressLine4;
    private String defendantAddressLine5;
    private String defendantPostCode;
}
