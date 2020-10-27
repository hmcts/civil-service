package uk.gov.hmcts.reform.unspec.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApplicantNotProceedingReason {

    private final String reason;
    private final String other;
}
