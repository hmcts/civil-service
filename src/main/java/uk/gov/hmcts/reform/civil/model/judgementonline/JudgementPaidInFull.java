package uk.gov.hmcts.reform.civil.model.judgementonline;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class JudgementPaidInFull {
    private LocalDate dateOfFullPaymentMade;
}
