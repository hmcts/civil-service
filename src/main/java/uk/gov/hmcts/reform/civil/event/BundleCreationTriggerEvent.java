package uk.gov.hmcts.reform.civil.event;

import lombok.Value;

@Value
public class BundleCreationTriggerEvent {

    Long caseId;
}
