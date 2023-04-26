package uk.gov.hmcts.reform.cmc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CourtDetermination {

    private PaymentIntention courtDecision;
    private PaymentIntention courtPaymentIntention;
    private String rejectionReason;
    private BigDecimal disposableIncome;
}
