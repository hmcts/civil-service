package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.finalorders.AppealList;
import uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList;

@Accessors(chain = true)
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FinalOrderAppeal {

    private AppealList list;
    private String otherText;
    private ApplicationAppealList applicationList;
    private AppealGrantedRefused appealGrantedDropdown;
    private AppealGrantedRefused appealRefusedDropdown;
}
