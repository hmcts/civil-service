package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder
public class DisclosureReport {

    private YesOrNo disclosureFormFiledAndServed;
    private YesOrNo disclosureProposalAgreed;
    private String draftOrderNumber;
}
