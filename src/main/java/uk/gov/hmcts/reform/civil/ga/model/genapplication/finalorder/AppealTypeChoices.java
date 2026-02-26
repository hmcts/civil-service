package uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.ga.enums.dq.PermissionToAppealTypes;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class AppealTypeChoices {

    private AppealTypeChoiceList appealChoiceOptionA;
    private AppealTypeChoiceList appealChoiceOptionB;
    private PermissionToAppealTypes assistedOrderAppealJudgeSelection;
    private PermissionToAppealTypes assistedOrderAppealJudgeSelectionRefuse;

    @JsonCreator
    AppealTypeChoices(@JsonProperty("assistedOrderAppealFirstOption") AppealTypeChoiceList appealChoiceOptionA,
                      @JsonProperty("assistedOrderAppealSecondOption") AppealTypeChoiceList appealChoiceOptionB,
                      @JsonProperty("assistedOrderAppealJudgeSelection") PermissionToAppealTypes assistedOrderAppealJudgeSelection,
                      @JsonProperty("assistedOrderAppealJudgeSelectionRefuse") PermissionToAppealTypes assistedOrderAppealJudgeSelectionRefuse
    ) {

        this.appealChoiceOptionA = appealChoiceOptionA;
        this.appealChoiceOptionB = appealChoiceOptionB;
        this.assistedOrderAppealJudgeSelection = assistedOrderAppealJudgeSelection;
        this.assistedOrderAppealJudgeSelectionRefuse = assistedOrderAppealJudgeSelectionRefuse;
    }

}
