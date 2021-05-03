package uk.gov.hmcts.reform.civil.service.data;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

@Data
@Builder
public class ExternalTaskInput {

    String caseId;
    CaseEvent caseEvent;
}
