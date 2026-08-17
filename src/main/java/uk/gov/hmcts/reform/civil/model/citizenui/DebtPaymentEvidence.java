package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.DebtPaymentOptions;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DebtPaymentEvidence {

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "DebtPaymentOptions"
    )
    private DebtPaymentOptions debtPaymentOption;
    @CCD(label = " ", searchable = false)
    private String provideDetails;
}
