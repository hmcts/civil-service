package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediationLip {

    private MediationDecision hasAgreedFreeMediation;
}
