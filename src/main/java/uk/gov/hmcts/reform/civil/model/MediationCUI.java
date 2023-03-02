package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;

@Data
@Builder
public class MediationCUI {
    private final MediationDecision hasAgreedFreeMediation;
}
