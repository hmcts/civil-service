package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CertOfSC {

    private LocalDate defendantFinalPaymentDate;
    private DebtPaymentEvidence debtPaymentEvidence;
    private Document proofOfDebtDoc;
}
