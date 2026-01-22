package uk.gov.hmcts.reform.civil.ga.event;

import lombok.Value;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;

@Value
public class DispatchBusinessProcessEvent {

    Long caseId;
    BusinessProcess businessProcess;
}
