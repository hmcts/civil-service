package uk.gov.hmcts.reform.civil.handler.tasks.variables;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Builder(toBuilder = true)
@AllArgsConstructor
@Data
public class HearingNoticeMessageVars implements MappableObject {

    private String hearingId;
    private String caseId;
    private boolean triggeredViaScheduler;

}
