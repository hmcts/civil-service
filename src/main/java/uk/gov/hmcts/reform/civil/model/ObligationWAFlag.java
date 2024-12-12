package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ObligationWAFlag {

    private YesOrNo unlessOrder;
    private YesOrNo stayACase;
    private YesOrNo liftAStay;
    private YesOrNo dismissCase;
    private YesOrNo preTrialChecklist;
    private YesOrNo generalOrder;
    private YesOrNo reserveJudgment;
    private YesOrNo other;
    private String obligationReason;
    private String obligationReasonDisplayValue;
    private String currentDate;
}
