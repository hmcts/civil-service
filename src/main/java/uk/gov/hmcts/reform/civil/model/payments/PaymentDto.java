package uk.gov.hmcts.reform.civil.model.payments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(SnakeCaseStrategy.class)
@JsonInclude(NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentDto {

    private String id;

    private String reference;

    private CurrencyCode currency;

    private String ccdCaseNumber;

    private String caseReference;

    private BigDecimal amount;

    private String paymentReference;

    private String paymentGroupReference;

    private String status;

    private String externalReference;

    private String customerReference;

    private List<StatusHistoryDto> statusHistories;

}
