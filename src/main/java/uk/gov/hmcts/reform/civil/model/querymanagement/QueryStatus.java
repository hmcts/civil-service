package uk.gov.hmcts.reform.civil.model.querymanagement;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
public class QueryStatus {

    private final String queryId;
    private final StatusType status;

}
