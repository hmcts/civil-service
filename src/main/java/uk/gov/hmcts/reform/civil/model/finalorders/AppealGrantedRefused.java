package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AppealGrantedRefused {

    private AppealChoiceSecondDropdown appealChoiceSecondDropdownA;
    private AppealChoiceSecondDropdown appealChoiceSecondDropdownB;
    private ApplicationAppealList circuitOrHighCourtList;
    private ApplicationAppealList circuitOrHighCourtListRefuse;
}
