package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DisclosureReport {

    @CCD(
            label = "Have you filed and served a disclosure report (Form N263) (see Civil Procedure Rules Part 31)?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo disclosureFormFiledAndServed;
    @CCD(
            label = "Have you agreed a proposal in relation to disclosure that meets the overriding objective?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo disclosureProposalAgreed;
    @CCD(
            label = "Please ensure this is contained within the proposed directions attached and specify the draft order number",
            showCondition = "disclosureProposalAgreed = \"Yes\"",
            searchable = false
    )
    private String draftOrderNumber;
}
