package uk.gov.hmcts.reform.civil.controllers.fees;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.service.FeesPaymentService;
import java.time.OffsetDateTime;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FeesPaymentController {

    public static final String FEES_PAYMENT_REQUEST_URL = "/fees/{feeType}/case/{caseReference}/payment";
    public static final String FEES_PAYMENT_STATUS_URL = "/fees/{feeType}/payment/{paymentReference}/status";
    private final FeesPaymentService feesPaymentService;

    @PostMapping(path = FEES_PAYMENT_REQUEST_URL, consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(summary = "Citizen UI will call this API and will get gov pay link for payment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful Gov pay link is created."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public ResponseEntity<CardPaymentStatusResponse> createGovPaymentRequest(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
        @PathVariable("feeType") FeeType feeType,
        @PathVariable("caseReference") String caseReference) {

        return new ResponseEntity<>(
            feesPaymentService.createGovPaymentRequest(feeType, caseReference, authorization),
            HttpStatus.OK
        );
    }

    @GetMapping(path = FEES_PAYMENT_STATUS_URL, produces = APPLICATION_JSON)
    @Operation(summary = "Citizen UI will call this API and will get status of gov pay payment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful Gov pay status."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public ResponseEntity<CardPaymentStatusResponse> getGovPaymentRequestStatus(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
        @PathVariable("feeType") FeeType feeType,
        @PathVariable("paymentReference") String paymentReference) {
        CardPaymentStatusResponse response = new CardPaymentStatusResponse().builder()
            .status("Success")
            .externalReference("lbh2ogknloh9p3b4lchngdfg63")
            .paymentReference("RC-1701-0909-0602-0418")
            .nextUrl("https://card.payments.service.gov.uk/secure/7b0716b2-40c4-413e-b62e-72c599c91960")
            .dateCreated(OffsetDateTime.parse("2023-11-27T13:15:06.313+00:00"))
            .build();
        return new ResponseEntity<>(
            response,
            HttpStatus.OK
        );
    }
}
