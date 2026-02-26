package uk.gov.hmcts.reform.cmc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class CourtDetermination {

    private PaymentIntention courtDecision;
    private PaymentIntention courtPaymentIntention;
    private String rejectionReason;
    private BigDecimal disposableIncome;
}
