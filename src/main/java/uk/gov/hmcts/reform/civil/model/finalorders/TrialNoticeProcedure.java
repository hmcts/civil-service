package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantDefendantNotAttending;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TrialNoticeProcedure {

    private FinalOrdersClaimantDefendantNotAttending list;
    private FinalOrdersClaimantDefendantNotAttending listClaimTwo;
    private FinalOrdersClaimantDefendantNotAttending listDef;
    private FinalOrdersClaimantDefendantNotAttending listDefTwo;
}
