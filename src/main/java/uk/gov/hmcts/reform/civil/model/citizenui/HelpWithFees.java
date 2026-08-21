package uk.gov.hmcts.reform.civil.model.citizenui;

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
public class HelpWithFees {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo helpWithFee;
    @CCD(label = " ", searchable = false)
    private String helpWithFeesReferenceNumber;

    public HelpWithFees copy() {
        return new HelpWithFees()
            .setHelpWithFee(this.helpWithFee)
            .setHelpWithFeesReferenceNumber(this.helpWithFeesReferenceNumber);
    }
}
