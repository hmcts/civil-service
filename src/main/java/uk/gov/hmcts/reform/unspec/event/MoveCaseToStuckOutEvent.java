package uk.gov.hmcts.reform.unspec.event;

import lombok.Value;

@Value
public class MoveCaseToStuckOutEvent {

    Long caseId;
}
