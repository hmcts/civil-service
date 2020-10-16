package uk.gov.hmcts.reform.unspec.event;

import lombok.Value;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;

@Value
public class DispatchBusinessProcessEvent {

    Long caseId;
    BusinessProcess businessProcess;
}
