package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder
public class DisclosureOfElectronicDocuments {

    private final YesOrNo reachedAgreement;
    private final YesOrNo agreementLikely;
    private final String reasonForNoAgreement;
}
