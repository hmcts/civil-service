package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.payments.response.CardPaymentServiceRequestResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class CardPaymentStatusResponse {

    private String externalReference;
    private String paymentReference;
    private String status;
    private String nextUrl;
    private OffsetDateTime dateCreated;
    private String errorCode;
    private String errorDescription;
    private String paymentFor;
    private BigDecimal paymentAmount;

    public static CardPaymentStatusResponse from(CardPaymentServiceRequestResponse cardPaymentServiceRequestResponse) {
        return new CardPaymentStatusResponse()
            .setPaymentReference(cardPaymentServiceRequestResponse.getPaymentReference())
            .setExternalReference(cardPaymentServiceRequestResponse.getExternalReference())
            .setStatus(cardPaymentServiceRequestResponse.getStatus())
            .setNextUrl(cardPaymentServiceRequestResponse.getNextUrl())
            .setDateCreated(cardPaymentServiceRequestResponse.getDateCreated());
    }
}
