package uk.gov.hmcts.reform.civil.model.querymanagement;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
public class LatestQuery {

    private final String queryId;
    private final YesOrNo isHearingRelated;
    private final YesOrNo isAdditionalQuestion;

}
