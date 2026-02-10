package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2SmallClaimsHearingLengthOther {

    private Integer trialLengthDays;
    private Integer trialLengthHours;
    private Integer trialLengthMinutes;
}
