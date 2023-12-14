package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HearingValuesRequest {

    private final Long caseReference;
    private final String hearingId;
}
