package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardPaymentStatusResponse {

    private String externalReference;
    private String paymentReference;
    private String status;
    private OffsetDateTime dateCreated;
    private String errorCode;
    private String errorDescription;
}
