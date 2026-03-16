package uk.gov.hmcts.reform.civil.client.payments.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.client.payments.models.FeeDto;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreditAccountPaymentRequest {
    private String accountNumber;
    private BigDecimal amount;
    private String caseReference;
    private String ccdCaseNumber;
    @Builder.Default
    private String currency = "GBP";
    private String customerReference;
    private String description;
    private String organisationName;
    // at the time of writing, permitted services are FPL, CMC, DIVORCE, PROBATE, FINREM and DIGITAL_BAR
    private String service;
    private String siteId;
    private FeeDto[] fees;
}
