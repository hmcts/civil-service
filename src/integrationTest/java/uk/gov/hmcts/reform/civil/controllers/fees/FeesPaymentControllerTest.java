package uk.gov.hmcts.reform.civil.controllers.fees;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataUpdateException;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.SRPbaDetails;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.UpdatePaymentStatusService;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.client.models.StatusHistoryDto;
import uk.gov.hmcts.reform.payments.request.CardPaymentServiceRequestDTO;
import uk.gov.hmcts.reform.payments.response.CardPaymentServiceRequestResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.controllers.fees.FeesPaymentController.FEES_PAYMENT_REQUEST_URL;
import static uk.gov.hmcts.reform.civil.controllers.fees.FeesPaymentController.FEES_PAYMENT_STATUS_URL;
import static uk.gov.hmcts.reform.civil.enums.FeeType.HEARING;

public class FeesPaymentControllerTest extends BaseIntegrationTest {

    private static final CardPaymentServiceRequestDTO CARD_PAYMENT_SERVICE_REQUEST
        = CardPaymentServiceRequestDTO.builder()
        .returnUrl("http://localhost:3001/hearing-payment-confirmation/1701090368574910")
        .language("en")
        .amount(new BigDecimal("232.00")).currency("GBP").build();

    @MockBean
    private PaymentsClient paymentsClient;
    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @MockBean
    private UpdatePaymentStatusService updatePaymentStatusService;

    @BeforeEach
    void before() {
        CaseDetails expectedCaseDetails = CaseDetails.builder().id(1701090368574910L)
            .data(Map.of(
                "hearingFeePBADetails",
                SRPbaDetails.builder().serviceReqReference("2023-1701090705688")
                    .fee(Fee.builder().calculatedAmountInPence(new BigDecimal("23200")).build())
                    .build(),
                "hearingFee",
                Fee.builder().calculatedAmountInPence(new BigDecimal("23200")).build()
            )).build();

        when(coreCaseDataService.getCase(1701090368574910L)).thenReturn(expectedCaseDetails);
    }

    @Test
    @SneakyThrows
    void shouldCreateGovPayPaymentUrlForServiceRequestPayment() {
        CardPaymentServiceRequestResponse response = buildServiceRequestResponse();

        when(paymentsClient.createGovPayCardPaymentRequest(
            "2023-1701090705688",
            BEARER_TOKEN,
            CARD_PAYMENT_SERVICE_REQUEST
        )).thenReturn(response);

        doPost(BEARER_TOKEN, "", FEES_PAYMENT_REQUEST_URL, HEARING.name(), "1701090368574910")
            .andExpect(content().json(toJson(CardPaymentStatusResponse.from(response))))
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({"Success", "Failed", "Pending", "Declined"})
    @SneakyThrows
    void shouldReturnServiceRequestPaymentStatus(String status) {
        PaymentDto response = buildGovPayCardPaymentStatusResponse(status);
        when(paymentsClient.getGovPayCardPaymentStatus("RC-1701-0909-0602-0418", BEARER_TOKEN))
            .thenReturn(response);
        doGet(BEARER_TOKEN, FEES_PAYMENT_STATUS_URL, HEARING.name(), "123", "RC-1701-0909-0602-0418")
            .andExpect(content().json(toJson(expectedResponse(status))))
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({"Success"})
    @SneakyThrows
    void shouldReturnServiceRequestPaymentStatusWhenExceptionInCaseDataUpdate(String status) {
        PaymentDto response = buildGovPayCardPaymentStatusResponse(status);
        when(paymentsClient.getGovPayCardPaymentStatus("RC-1701-0909-0602-0418", BEARER_TOKEN))
            .thenReturn(response);
        doThrow(new CaseDataUpdateException()).when(updatePaymentStatusService).updatePaymentStatus(any(), any(), any());
        doGet(BEARER_TOKEN, FEES_PAYMENT_STATUS_URL, HEARING.name(), "123", "RC-1701-0909-0602-0418")
            .andExpect(content().json(toJson(expectedResponse(status))))
            .andExpect(status().isOk());
    }

    private PaymentDto buildGovPayCardPaymentStatusResponse(String status) {
        return PaymentDto.builder()
            .externalReference("lbh2ogknloh9p3b4lchngdfg63")
            .reference("RC-1701-0909-0602-0418")
            .status(status)
            .currency("GBP")
            .amount(new BigDecimal(200))
            .statusHistories(getStatusHistories(status))
            .build();
    }

    private CardPaymentStatusResponse expectedResponse(String status) {
        CardPaymentStatusResponse.CardPaymentStatusResponseBuilder payment
            = CardPaymentStatusResponse.builder()
            .paymentReference("RC-1701-0909-0602-0418")
            .status(status)
            .paymentAmount(new BigDecimal(200))
            .paymentFor("hearing");

        if (status.equals("Failed")) {
            payment.errorCode("P0030").errorDescription("Payment was cancelled by the user");
        }
        return payment.build();
    }

    private StatusHistoryDto[] getStatusHistories(String status) {

        StatusHistoryDto initiatedHistory = StatusHistoryDto.builder().status("Initiated").build();
        StatusHistoryDto failedHistory = StatusHistoryDto.builder().status("Failed")
            .errorCode("P0030")
            .errorMessage("Payment was cancelled by the user").build();
        List<StatusHistoryDto> histories = new ArrayList<>();
        histories.add(initiatedHistory);
        if (status.equals("Failed")) {
            histories.add(failedHistory);
        } else {
            histories.add(StatusHistoryDto.builder().status(status).build());
        }
        StatusHistoryDto[] result = new StatusHistoryDto[histories.size()];
        return histories.toArray(result);
    }

    private CardPaymentServiceRequestResponse buildServiceRequestResponse() {
        return new CardPaymentServiceRequestResponse(
            "lbh2ogknloh9p3b4lchngdfg63",
            "RC-1701-0909-0602-0418",
            "Initiated",
            "https://card.payments.service.gov.uk/secure/7b0716b2-40c4-413e-b62e-72c599c91960",
            OffsetDateTime.parse("2023-11-27T13:15:06.313+00:00")
        );
    }

}
