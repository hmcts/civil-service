package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppealGrantedRefused {

    private AppealChoiceSecondDropdown appealChoiceSecondDropdownA;
    private AppealChoiceSecondDropdown appealChoiceSecondDropdownB;
    private ApplicationAppealList circuitOrHighCourtList;
    private ApplicationAppealList circuitOrHighCourtListRefuse;
}
