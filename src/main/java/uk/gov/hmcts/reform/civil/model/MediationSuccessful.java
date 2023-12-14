package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediationSuccessful {

    private LocalDate mediationSettlementAgreedAt;
    private MediationAgreementDocument mediationAgreement;
}
