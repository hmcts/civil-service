package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisclosureOfElectronicDocuments {

    private YesOrNo reachedAgreement;
    private YesOrNo agreementLikely;
    private String reasonForNoAgreement;
}
