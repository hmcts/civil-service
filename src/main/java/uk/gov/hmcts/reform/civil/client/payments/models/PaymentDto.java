package uk.gov.hmcts.reform.civil.client.payments.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    @JsonProperty("_links")
    private LinksDto links;
    private String accountNumber;
    private BigDecimal amount;
    private String caseReference;
    private String ccdCaseNumber;
    private String channel;
    @Builder.Default
    private String currency = "GBP";
    private String customerReference;
    private OffsetDateTime dateCreated;
    private OffsetDateTime dateUpdated;
    private String description;
    private String externalProvider;
    private String externalReference;
    private FeeDto[] fees;
    private String giroSlipNo;
    private String id;
    private String method;
    private String organisationName;
    private String paymentGroupReference;
    private String paymentReference;
    private String reference;
    private String reportedDateOffline;
    // at the time of writing, permitted services are FPL, CMC, DIVORCE, PROBATE, FINREM and DIGITAL_BAR
    private String serviceName;
    private String siteId;
    private String status;
    private StatusHistoryDto[] statusHistories;
}
