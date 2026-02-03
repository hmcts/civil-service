package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.DebtPaymentOptions;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DebtPaymentEvidence {

    private DebtPaymentOptions debtPaymentOption;
    private String provideDetails;
}
