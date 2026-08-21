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
public class FinancialDetailsLiP {

    @CCD(label = "Does your partner receive a pension?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo partnerPensionLiP;
    @CCD(label = "Is your partner disabled?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo partnerDisabilityLiP;
    @CCD(label = "Is your partner severely disabled?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo partnerSevereDisabilityLiP;
    @CCD(label = "Children aged 16 to 19 living with you", searchable = false)
    private String childrenEducationLiP;
}
