package uk.gov.hmcts.reform.civil.model.servicerequest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * The fields civil-service reads from the payment service's {@code GET /cases/{ccdCaseNumber}/paymentgroups}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CasePaymentGroupsResponse {

    @JsonProperty("payment_groups")
    private List<PaymentGroup> paymentGroups;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentGroup {

        @JsonProperty("payment_group_reference")
        private String paymentGroupReference;

        /**
         * "Not paid", "Partially paid", "Paid" or "Disputed".
         */
        @JsonProperty("service_request_status")
        private String serviceRequestStatus;

        @JsonProperty("fees")
        private List<Fee> fees;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fee {

        @JsonProperty("code")
        private String code;

        @JsonProperty("calculated_amount")
        private BigDecimal calculatedAmount;
    }
}
