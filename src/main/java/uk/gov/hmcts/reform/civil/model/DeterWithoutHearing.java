package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeterWithoutHearing {

    @CCD(
            label = "**Do you consider that this claim is suitable for determination without a hearing** i.e. by a Judge reading and considering the case papers, witness statements and other documents filed by the parties, making a decision, and giving a note of reasons for that decision? ",
            searchable = false,
            typeOverride = FieldType.Label
    )
    private String deterWithoutHearingLabel;
    @CCD(label = "Determination without hearing?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo deterWithoutHearingYesNo;
    @CCD(label = "Tell us why", searchable = false, typeOverride = FieldType.TextArea)
    private String deterWithoutHearingWhyNot;
}
