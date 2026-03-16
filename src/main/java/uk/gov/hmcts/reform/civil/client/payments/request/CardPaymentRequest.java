package uk.gov.hmcts.reform.civil.client.payments.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.client.payments.models.FeeDto;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardPaymentRequest {
    private BigDecimal amount;
    private String caseReference;
    private String ccdCaseNumber;
    private String channel;
    @Builder.Default
    private String currency = "GBP";
    private String description;
    private FeeDto[] fees;
    private String provider;
    // at the time of writing, permitted services are FPL, CMC, DIVORCE, PROBATE, FINREM and DIGITAL_BAR
    private String caseType;
}
