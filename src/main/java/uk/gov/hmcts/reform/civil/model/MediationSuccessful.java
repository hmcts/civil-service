package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MediationSuccessful {

    private LocalDate mediationSettlementAgreedAt;
    private MediationAgreementDocument mediationAgreement;
}
