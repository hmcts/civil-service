package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FinalOrderAppeal {
    private AppealList list;
    private String otherText;
    private ApplicationAppealList applicationList;
    private String reasonsText;
}
