package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;

import java.time.LocalDateTime;

@Setter
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
public class SRPbaDetails {

    private DynamicList applicantsPbaAccounts;
    private String pbaReference;
    private Fee fee;
    private PaymentDetails paymentDetails;
    private LocalDateTime paymentSuccessfulDate;
    private String serviceReqReference;

    @JsonCreator
    SRPbaDetails(@JsonProperty("applicantsPbaAccounts") DynamicList applicantsPbaAccounts,
                 @JsonProperty("pbaReference") String pbaReference,
                 @JsonProperty("fee") Fee fee,
                 @JsonProperty("paymentDetails") PaymentDetails paymentDetails,
                 @JsonProperty("paymentSuccessfulDate") LocalDateTime paymentSuccessfulDate,
                 @JsonProperty("serviceRequestReference") String serviceReqReference) {
        this.applicantsPbaAccounts = applicantsPbaAccounts;
        this.pbaReference = pbaReference;
        this.fee = fee;
        this.paymentDetails = paymentDetails;
        this.paymentSuccessfulDate = paymentSuccessfulDate;
        this.serviceReqReference = serviceReqReference;

    }
}
