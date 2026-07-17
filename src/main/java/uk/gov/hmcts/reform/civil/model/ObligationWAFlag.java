package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObligationWAFlag {

    @CCD(label = " ", searchable = false)
    private String obligationReason;
    @CCD(label = " ", searchable = false)
    private String obligationReasonDisplayValue;
    @CCD(label = " ", searchable = false)
    private String currentDate;
}
