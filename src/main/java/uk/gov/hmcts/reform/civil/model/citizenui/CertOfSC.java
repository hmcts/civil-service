package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CertOfSC {

    private LocalDate defendantFinalPaymentDate;
    private DebtPaymentEvidence debtPaymentEvidence;
    private List<Element<Document>> proofOfDebtDoc;
}
