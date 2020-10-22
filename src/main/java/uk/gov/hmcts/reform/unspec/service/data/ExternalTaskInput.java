package uk.gov.hmcts.reform.unspec.service.data;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;

@Data
@Builder
public class ExternalTaskInput {

    String caseId;
    CaseEvent caseEvent;
}
