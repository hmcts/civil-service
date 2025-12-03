package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList;

@Accessors(chain = true)
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
