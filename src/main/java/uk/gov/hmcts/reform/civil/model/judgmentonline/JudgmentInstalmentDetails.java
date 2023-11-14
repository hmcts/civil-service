package uk.gov.hmcts.reform.civil.model.judgmentonline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class JudgmentInstalmentDetails {

    private String instalmentAmount;
    private PaymentFrequency paymentFrequency;
    private LocalDate firstInstalmentDate;
}
