package uk.gov.hmcts.reform.civil.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalTaskInput {

    String caseId;
    CaseEvent caseEvent;
    String generalAppParentCaseLink;
    Boolean triggeredViaScheduler;
}
