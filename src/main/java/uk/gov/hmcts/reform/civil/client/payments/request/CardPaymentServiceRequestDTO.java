package uk.gov.hmcts.reform.civil.client.payments.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardPaymentServiceRequestDTO {

    @JsonProperty("language")
    private String language;
    @JsonProperty("amount")
    private BigDecimal amount;
    @Builder.Default
    @JsonProperty("currency")
    private String currency = "GBP";
    @JsonProperty("return-url")
    private String returnUrl;
}
