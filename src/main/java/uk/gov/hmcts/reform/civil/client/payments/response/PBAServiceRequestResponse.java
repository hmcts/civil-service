package uk.gov.hmcts.reform.civil.client.payments.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PBAServiceRequestResponse {

    @JsonProperty("payment_reference")
    private String paymentReference;

    @JsonProperty("status")
    private String status;

    @JsonProperty("date_created")
    private String dateCreated;
}
