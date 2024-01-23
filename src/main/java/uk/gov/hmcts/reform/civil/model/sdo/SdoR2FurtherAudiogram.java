package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2FurtherAudiogram {

    private String sdoClaimantShallUndergoTxt;
    private Date sdoClaimantShallUndergoDate;
    private String sdoServiceReportTxt;
    private Date sdoServiceReportDate;

}
