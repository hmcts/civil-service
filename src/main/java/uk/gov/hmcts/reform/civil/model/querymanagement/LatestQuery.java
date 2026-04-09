package uk.gov.hmcts.reform.civil.model.querymanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LatestQuery {

    private String queryId;
    private YesOrNo isHearingRelated;
    private YesOrNo isWelsh;

}
