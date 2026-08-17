package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2TrialHearingLengthOther {

    @CCD(label = "Days", hint = "For example,2", searchable = false)
    private Integer trialLengthDays;
    @CCD(label = "Hours", hint = "For example,4", searchable = false)
    private Integer trialLengthHours;
    @CCD(label = "Minutes", hint = "For example,0", searchable = false)
    private Integer trialLengthMinutes;
}
