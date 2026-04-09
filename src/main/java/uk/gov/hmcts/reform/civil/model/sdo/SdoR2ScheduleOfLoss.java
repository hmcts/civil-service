package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2ScheduleOfLoss {

    private String sdoR2ScheduleOfLossClaimantText;
    private LocalDate sdoR2ScheduleOfLossClaimantDate;
    private String sdoR2ScheduleOfLossDefendantText;
    private LocalDate sdoR2ScheduleOfLossDefendantDate;
    private YesOrNo isClaimForPecuniaryLoss;
    private String sdoR2ScheduleOfLossPecuniaryLossTxt;

}
