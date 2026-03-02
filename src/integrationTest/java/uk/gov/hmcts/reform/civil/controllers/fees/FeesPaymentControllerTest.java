package uk.gov.hmcts.reform.civil.controllers.fees;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataUpdateException;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.UpdatePaymentStatusService;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.SRPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.PaymentStatusRetryService;
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
import static uk.gov.hmcts.reform.civil.controllers.fees.FeesPaymentController.GA_FEES_PAYMENT_REQUEST_URL;
import static uk.gov.hmcts.reform.civil.controllers.fees.FeesPaymentController.GA_FEES_PAYMENT_STATUS_URL;
import static uk.gov.hmcts.reform.civil.enums.FeeType.HEARING;

public class FeesPaymentControllerTest extends BaseIntegrationTest {

    private static final Long CASE_REFERENCE = 1701090368574910L;
    private static final String HEARING_PAYMENT_RETURN_URL =
        "http://localhost:3001/hearing-payment-confirmation/" + CASE_REFERENCE;

    @MockBean
    private PaymentsClient paymentsClient;
    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @MockBean
    private PaymentStatusRetryService paymentStatusRetryService;
    @MockBean
    private GaCoreCaseDataService gaCoreCaseDataService;
    @MockBean
    private UpdatePaymentStatusService updatePaymentStatusService;

    @BeforeEach
    void before() {
        CaseDetails expectedCaseDetails = CaseDetails.builder().id(CASE_REFERENCE)
                .data(Map.of(
                        "hearingFeePBADetails",
                        new SRPbaDetails().setServiceReqReference("2023-1701090705688")
                                .setFee(new Fee().setCalculatedAmountInPence(new BigDecimal("23200"))),
                        "hearingFee",
                        new Fee().setCalculatedAmountInPence(new BigDecimal("23200"))
                )).build();

        when(coreCaseDataService.getCase(CASE_REFERENCE)).thenReturn(expectedCaseDetails);
    }

    @Test
    @SneakyThrows
    void shouldCreateGovPayPaymentUrlForServiceRequestPayment() {
        CardPaymentServiceRequestResponse response = buildServiceRequestResponse();

        when(paymentsClient.createGovPayCardPaymentRequest(
                "2023-1701090705688",
                BEARER_TOKEN,
                cardPaymentRequestWith(HEARING_PAYMENT_RETURN_URL)
        )).thenReturn(response);

        doPost(BEARER_TOKEN, "", FEES_PAYMENT_REQUEST_URL, HEARING.name(), CASE_REFERENCE)
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
        doThrow(new CaseDataUpdateException()).when(paymentStatusRetryService).updatePaymentStatus(any(), any(), any(CardPaymentStatusResponse.class));
        doGet(BEARER_TOKEN, FEES_PAYMENT_STATUS_URL, HEARING.name(), "123", "RC-1701-0909-0602-0418")
                .andExpect(content().json(toJson(expectedResponse(status))))
                .andExpect(status().isOk());
    }

    @Nested
    class GeneralApplicationTests {

        private static final Long GA_CASE_REFERENCE = 2801090368574910L;
        private static final String GA_RETURN_URL =
            "http://localhost:3001/general-application/payment-confirmation/"
                + CASE_REFERENCE
                + "/gaid/"
                + GA_CASE_REFERENCE;

        @BeforeEach
        void before() {
            CaseDetails expectedCaseDetails = CaseDetails.builder().id(GA_CASE_REFERENCE)
                .data(Map.of(
                    "generalAppPBADetails",
                    GAPbaDetails.builder().serviceReqReference("2023-1701090705688")
                        .fee(new Fee().setCalculatedAmountInPence(new BigDecimal("23200")))
                        .build(),
                    "generalAppFee",
                    new Fee().setCalculatedAmountInPence(new BigDecimal("23200")),
                    "parentCaseReference",
                    CASE_REFERENCE
                )).build();

            when(gaCoreCaseDataService.getCase(GA_CASE_REFERENCE)).thenReturn(expectedCaseDetails);
        }

        @Test
        @SneakyThrows
        void shouldCreateGovPayPaymentUrlForServiceRequestPayment() {
            final CardPaymentServiceRequestResponse response = buildServiceRequestResponse();

            when(paymentsClient.createGovPayCardPaymentRequest(
                "2023-1701090705688",
                BEARER_TOKEN,
                cardPaymentRequestWith(GA_RETURN_URL)
            )).thenReturn(response);

            doPost(BEARER_TOKEN, "", GA_FEES_PAYMENT_REQUEST_URL, GA_CASE_REFERENCE)
                .andExpect(content().json(toJson(CardPaymentStatusResponse.from(response))))
                .andExpect(status().isOk());
        }

        @ParameterizedTest
        @CsvSource({"Success", "Failed", "Pending", "Declined"})
        @SneakyThrows
        void shouldReturnServiceRequestPaymentStatus(String status) {
            final PaymentDto response = buildGovPayCardPaymentStatusResponse(status);
            when(paymentsClient.getGovPayCardPaymentStatus("RC-1701-0909-0602-0418", BEARER_TOKEN))
                .thenReturn(response);
            doGet(BEARER_TOKEN, GA_FEES_PAYMENT_STATUS_URL, "123", "RC-1701-0909-0602-0418")
                .andExpect(content().json(toJson(gaExpectedResponse(status))))
                .andExpect(status().isOk());
        }

        @ParameterizedTest
        @CsvSource({"Success"})
        @SneakyThrows
        void shouldReturnServiceRequestPaymentStatusWhenExceptionInCaseDataUpdate(String status) {
            final PaymentDto response = buildGovPayCardPaymentStatusResponse(status);
            when(paymentsClient.getGovPayCardPaymentStatus("RC-1701-0909-0602-0418", BEARER_TOKEN))
                .thenReturn(response);
            doThrow(new CaseDataUpdateException()).when(updatePaymentStatusService).updatePaymentStatus(any(), any());
            doGet(BEARER_TOKEN, GA_FEES_PAYMENT_STATUS_URL, "123", "RC-1701-0909-0602-0418")
                .andExpect(content().json(toJson(gaExpectedResponse(status))))
                .andExpect(status().isOk());
        }

        private CardPaymentStatusResponse gaExpectedResponse(String status) {
            final CardPaymentStatusResponse payment = new CardPaymentStatusResponse()
                .setPaymentReference("RC-1701-0909-0602-0418")
                .setStatus(status)
                .setPaymentAmount(new BigDecimal(200));

            if (status.equals("Failed")) {
                payment.setErrorCode("P0030").setErrorDescription("Payment was cancelled by the user");
            }
            return payment;
        }
    }

    private CardPaymentServiceRequestDTO cardPaymentRequestWith(String returnUrl) {
        return CardPaymentServiceRequestDTO.builder()
            .returnUrl(returnUrl)
            .language("en")
            .amount(new BigDecimal("232.00")).currency("GBP").build();
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
        CardPaymentStatusResponse payment = new CardPaymentStatusResponse()
                .setPaymentReference("RC-1701-0909-0602-0418")
                .setStatus(status)
                .setPaymentAmount(new BigDecimal(200))
                .setPaymentFor("hearing");

        if (status.equals("Failed")) {
            payment.setErrorCode("P0030").setErrorDescription("Payment was cancelled by the user");
        }
        return payment;
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
