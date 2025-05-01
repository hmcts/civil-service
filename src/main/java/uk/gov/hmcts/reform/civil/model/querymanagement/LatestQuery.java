package uk.gov.hmcts.reform.civil.model.querymanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class LatestQuery {

    private String queryId;
    private YesOrNo isHearingRelated;

}
