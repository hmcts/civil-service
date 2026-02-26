package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceRequestUpdateDto {

    @JsonProperty("service_request_reference")
    private String serviceRequestReference;
    @JsonProperty("ccd_case_number")
    private String ccdCaseNumber;
    @JsonProperty("service_request_amount")
    private String serviceRequestAmount;
    @JsonProperty("service_request_status")
    private String serviceRequestStatus;
    @JsonProperty("payment")
    private PaymentDto payment;

}
