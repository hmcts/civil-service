package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.exceptions.PaymentsApiException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.SRPbaDetails;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.FeeType.HEARING;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FeesPaymentService.class, CoreCaseDataService.class, PaymentStatusService.class,
    PaymentsClient.class, CaseDetailsConverter.class, PinInPostConfiguration.class, ObjectMapper.class})
@EnableRetry
class FeesPaymentServiceTest {

    private static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3k"
        + "rV2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJzb2xpY2l0b3JAZXhhbXBsZS5jb20iLCJhdXRoX2xldmVsIjowLC"
        + "JhdWRpdFRyYWNraW5nSWQiOiJiNGJmMjJhMi02ZDFkLTRlYzYtODhlOS1mY2NhZDY2NjM2ZjgiLCJpc3MiOiJodHRwOi8vZnItYW06ODA4M"
        + "C9vcGVuYW0vb2F1dGgyL2htY3RzIiwidG9rZW5OYW1lIjoiYWNjZXNzX3Rva2VuIiwidG9rZW5fdHlwZSI6IkJlYXJlciIsImF1dGhHcmFu"
        + "dElkIjoiZjExMTk3MGQtMzQ3MS00YjY3LTkxMzYtZmYxYzk0MjMzMTZmIiwiYXVkIjoieHVpX3dlYmFwcCIsIm5iZiI6MTU5NDE5NzI3NCw"
        + "iZ3JhbnRfdHlwZSI6ImF1dGhvcml6YXRpb25fY29kZSIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJyb2xlcyIsImNyZWF0ZS11c2"
        + "VyIiwibWFuYWdlLXVzZXIiXSwiYXV0aF90aW1lIjoxNTk0MTk3MjczMDAwLCJyZWFsbSI6Ii9obWN0cyIsImV4cCI6MTU5NDIyNjA3NCwia"
        + "WF0IjoxNTk0MTk3Mjc0LCJleHBpcmVzX2luIjoyODgwMCwianRpIjoiYTJmNThmYzgtMmIwMy00M2I0LThkOTMtNmU0NWQyZTU0OTcxIn0."
        + "PTWXIvTTw54Ob1XYdP_x4l5-ITWDdbAY3-IPAPFkHDmjKgEVweabxrDIp2_RSoAcyZgza8LqJSTc00-_RzZ079nyl9pARy08BpljLZCmYdo"
        + "F2RO8CHuEVagF-SQdL37d-4pJPIMRChO0AmplBj1qMtVbuRd3WGNeUvoCtStdviFwlxvzRnLdHKwCi6AQHMaw1V9n9QyU9FxNSbwmNsCDt7"
        + "k02vLJDY9fLCsFYy5iWGCjb8lD1aX1NTv7jz2ttNNv7-smqp6L3LSSD_LCZMpf0h_3n5RXiv-N3vNpWe4ZC9u0AWQdHEE9QlKTZlsqwKSog"
        + "3yJWhyxAamdMepgW7Z8jQ";

    private static final CardPaymentServiceRequestDTO CARD_PAYMENT_SERVICE_REQUEST
        = CardPaymentServiceRequestDTO.builder()
        .returnUrl("http://localhost:3001/hearing-payment-confirmation/1701090368574910")
        .language("English")
        .amount(new BigDecimal("232.00")).currency("GBP").build();

    @Autowired
    private FeesPaymentService feesPaymentService;
    @MockBean
    private PaymentsClient paymentsClient;
    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @Autowired
    private CaseDetailsConverter caseDetailsConverter;
    private ObjectMapper objectMapper = new ObjectMapper();
    @MockBean
    private PinInPostConfiguration pinInPostConfiguration;

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
        when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn("http://localhost:3001");
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

        CardPaymentStatusResponse govPaymentRequest = feesPaymentService.createGovPaymentRequest(
            HEARING,
            "1701090368574910",
            BEARER_TOKEN
        );
        assertThat(govPaymentRequest).isEqualTo(CardPaymentStatusResponse.from(response));

    }

    @Test
    void shouldRetryCreatePaymentsApiWhenInternalServerErrorThrown() {
        when(paymentsClient.createGovPayCardPaymentRequest(
            "2023-1701090705688",
            BEARER_TOKEN,
            CARD_PAYMENT_SERVICE_REQUEST
        )).thenThrow(FeignException.InternalServerError.class);

        assertThrows(
            PaymentsApiException.class,
            () -> feesPaymentService.createGovPaymentRequest(FeeType.HEARING, "1701090368574910", BEARER_TOKEN)
        );

        verify(paymentsClient, times(3)).createGovPayCardPaymentRequest(
            "2023-1701090705688",
            BEARER_TOKEN,
            CARD_PAYMENT_SERVICE_REQUEST
        );
    }

    @Test
    void shouldNotRetryCreatePaymentsApiWhenExceptionOtherThanInternalServerIsThrown() {
        when(paymentsClient.createGovPayCardPaymentRequest(
            "2023-1701090705688",
            BEARER_TOKEN,
            CARD_PAYMENT_SERVICE_REQUEST
        )).thenThrow(FeignException.NotImplemented.class);

        assertThrows(
            PaymentsApiException.class,
            () -> feesPaymentService.createGovPaymentRequest(FeeType.HEARING, "1701090368574910", BEARER_TOKEN)
        );

        verify(paymentsClient).createGovPayCardPaymentRequest(
            "2023-1701090705688",
            BEARER_TOKEN,
            CARD_PAYMENT_SERVICE_REQUEST
        );
    }

    @Test
    void shouldFailOnCreatePaymentsApiTwiceThenHaveSuccessfulRetry() {
        CardPaymentServiceRequestResponse response = buildServiceRequestResponse();

        when(paymentsClient.createGovPayCardPaymentRequest(
            "2023-1701090705688",
            BEARER_TOKEN,
            CARD_PAYMENT_SERVICE_REQUEST
        )).thenThrow(FeignException.InternalServerError.class)
            .thenThrow(FeignException.InternalServerError.class)
            .thenReturn(response);

        CardPaymentStatusResponse govPaymentRequest =
            feesPaymentService.createGovPaymentRequest(FeeType.HEARING, "1701090368574910", BEARER_TOKEN);

        assertThat(govPaymentRequest).isEqualTo(CardPaymentStatusResponse.from(response));

        verify(paymentsClient, times(3)).createGovPayCardPaymentRequest(
            "2023-1701090705688",
            BEARER_TOKEN,
            CARD_PAYMENT_SERVICE_REQUEST
        );
    }

    @ParameterizedTest
    @CsvSource({"Success", "Failed", "Pending", "Declined"})
    @SneakyThrows
    void shouldReturnServiceRequestPaymentStatus(String status) {
        PaymentDto response = buildGovPayCardPaymentStatusResponse(status);
        when(paymentsClient.getGovPayCardPaymentStatus("RC-1701-0909-0602-0418", BEARER_TOKEN))
            .thenReturn(response);
        CardPaymentStatusResponse govPaymentRequestStatus = feesPaymentService.getGovPaymentRequestStatus(
            HEARING,
            "RC-1701-0909-0602-0418",
            BEARER_TOKEN
        );

        assertThat(govPaymentRequestStatus).isEqualTo(expectedResponse(status));
    }

    @Test
    void shouldRetryPaymentsApiWhenInternalServerErrorThrown() {
        when(paymentsClient.getGovPayCardPaymentStatus("RC-1701-0909-0602-0418", BEARER_TOKEN))
            .thenThrow(FeignException.InternalServerError.class);

        assertThrows(
            PaymentsApiException.class,
            () -> feesPaymentService.getGovPaymentRequestStatus(FeeType.HEARING, "RC-1701-0909-0602-0418", BEARER_TOKEN)
        );

        verify(paymentsClient, times(3)).getGovPayCardPaymentStatus("RC-1701-0909-0602-0418", BEARER_TOKEN);
    }

    @Test
    void shouldNotRetryPaymentsApiWhenExceptionOtherThanInternalServerIsThrown() {
        when(paymentsClient.getGovPayCardPaymentStatus("RC-1701-0909-0602-0418", BEARER_TOKEN))
            .thenThrow(FeignException.NotImplemented.class);

        assertThrows(
            PaymentsApiException.class,
            () -> feesPaymentService.getGovPaymentRequestStatus(FeeType.HEARING, "RC-1701-0909-0602-0418", BEARER_TOKEN
            )
        );

        verify(paymentsClient).getGovPayCardPaymentStatus("RC-1701-0909-0602-0418", BEARER_TOKEN);
    }

    @Test
    void shouldFailOnPaymentsApiTwiceThenHaveSuccessfulRetry() {
        when(paymentsClient.getGovPayCardPaymentStatus("RC-1701-0909-0602-0418", BEARER_TOKEN))
            .thenThrow(FeignException.InternalServerError.class)
            .thenThrow(FeignException.InternalServerError.class)
            .thenReturn(buildGovPayCardPaymentStatusResponse("Success"));

        feesPaymentService.getGovPaymentRequestStatus(FeeType.HEARING, "RC-1701-0909-0602-0418", BEARER_TOKEN);

        verify(paymentsClient, times(3))
            .getGovPayCardPaymentStatus("RC-1701-0909-0602-0418", BEARER_TOKEN);
    }

    private PaymentDto buildGovPayCardPaymentStatusResponse(String status) {
        return PaymentDto.builder()
            .externalReference("lbh2ogknloh9p3b4lchngdfg63")
            .paymentReference("RC-1701-0909-0602-0418")
            .status(status)
            .currency("GBP")
            .dateCreated(OffsetDateTime.parse("2023-11-27T13:15:06.313+00:00"))
            .statusHistories(getStatusHistories(status))
            .build();
    }

    private StatusHistoryDto[] getStatusHistories(String status) {

        StatusHistoryDto initiatedHistory = StatusHistoryDto.builder().status("Initiated").build();
        StatusHistoryDto successHistory = StatusHistoryDto.builder().status("Success").build();
        StatusHistoryDto failedHistory = StatusHistoryDto.builder().status("Failed")
            .errorCode("CA-E0001")
            .errorMessage("Payment request failed. PBA account accountName have insufficient funds available").build();
        StatusHistoryDto declinedHistory = StatusHistoryDto.builder().status("Declined")
            .errorCode("CA-E0003").errorMessage("Your account is on hold").build();
        StatusHistoryDto pendingHistory = StatusHistoryDto.builder().status("Pending")
            .errorCode("CA-E0004").errorMessage("Your account is deleted").build();
        List<StatusHistoryDto> histories = new ArrayList<>();
        histories.add(initiatedHistory);
        if (status.equals("Success")) {
            histories.add(successHistory);
        } else if (status.equals("Failed")) {
            histories.add(failedHistory);
        } else if (status.equals("Declined")) {
            histories.add(declinedHistory);
        } else {
            histories.add(pendingHistory);
        }
        StatusHistoryDto[] result = new StatusHistoryDto[histories.size()];
        return histories.toArray(result);
    }

    private CardPaymentStatusResponse expectedResponse(String status) {
        CardPaymentStatusResponse.CardPaymentStatusResponseBuilder payment
            = CardPaymentStatusResponse.builder()
            .paymentReference("RC-1701-0909-0602-0418")
            .externalReference("lbh2ogknloh9p3b4lchngdfg63")
            .status(status)
            .dateCreated(OffsetDateTime.parse("2023-11-27T13:15:06.313+00:00"));

        if (status.equals("Failed")) {
            payment.errorCode("CA-E0001")
                .errorDescription("Payment request failed. PBA account accountName have insufficient funds available");
        } else if (status.equals("Declined")) {
            payment.errorCode("CA-E0003")
                .errorDescription("Your account is on hold");
        } else if (status.equals("Pending")) {
            payment.errorCode("CA-E0004")
                .errorDescription("Your account is deleted");
        }
        return payment.build();
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
