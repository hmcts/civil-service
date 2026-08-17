package uk.gov.hmcts.reform.civil.model.judgmentonline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgmentAddress {

    @CCD(label = " ", searchable = false)
    private String defendantAddressLine1;
    @CCD(label = " ", searchable = false)
    private String defendantAddressLine2;
    @CCD(label = " ", searchable = false)
    private String defendantAddressLine3;
    @CCD(label = " ", searchable = false)
    private String defendantAddressLine4;
    @CCD(label = " ", searchable = false)
    private String defendantAddressLine5;
    @CCD(label = " ", searchable = false)
    private String defendantPostCode;
}
