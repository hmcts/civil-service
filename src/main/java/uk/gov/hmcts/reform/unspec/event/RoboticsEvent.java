package uk.gov.hmcts.reform.unspec.event;

import lombok.Value;
import uk.gov.hmcts.reform.unspec.model.CaseData;

@Value
public class RoboticsEvent {

    CaseData caseData;
}
