package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Setter
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class SRPbaDetails {

    @CCD(label = "Claimant solicitor PBA accounts", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList applicantsPbaAccounts;
    @CCD(label = "PBA Reference", searchable = false)
    private String pbaReference;
    @CCD(label = "Hearing Notice fee", searchable = false)
    private Fee fee;
    @CCD(label = "Details of PBA payment", searchable = false)
    private PaymentDetails paymentDetails;
    @CCD(label = "Payment successful date", searchable = false)
    private LocalDateTime paymentSuccessfulDate;
    @CCD(ignore = true)
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

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Service Request Reference", searchable = false)
  private String serviceRequestReference;
  // ==== end synthesised definition-only fields ====
}
