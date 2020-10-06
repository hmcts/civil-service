package uk.gov.hmcts.reform.unspec.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus;

@Data
@Builder
public class BusinessProcess {

    private final String processInstanceId;
    private final BusinessProcessStatus status;
    private final String activityId;
}
