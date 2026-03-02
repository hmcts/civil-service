package uk.gov.hmcts.reform.civil.ga.service;

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
import uk.gov.hmcts.reform.civil.exceptions.PaymentsApiException;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.service.PaymentStatusService;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.client.models.StatusHistoryDto;
import uk.gov.hmcts.reform.payments.request.CardPaymentServiceRequestDTO;
import uk.gov.hmcts.reform.payments.response.CardPaymentServiceRequestResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {GaFeesPaymentService.class, GaCoreCaseDataService.class, PaymentStatusService.class,
    PaymentsClient.class, CaseDetailsConverter.class, ObjectMapper.class})
@EnableRetry
class GaFeesPaymentServiceTest {

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
        .returnUrl("${cui-front-end.url}/general-application/payment-confirmation/1701090368574910/gaid/2801090368574910")
        .language("en")
        .amount(new BigDecimal("232.00")).currency("GBP").build();

    @Autowired
    private GaFeesPaymentService feesPaymentService;

    @MockBean
    private PaymentsClient paymentsClient;

    @MockBean
    private GaCoreCaseDataService coreCaseDataService;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockBean
    private UpdatePaymentStatusService updatePaymentStatusService;

    private GeneralApplicationCaseData caseData;

    @BeforeEach
    void before() {
        caseData = new GeneralApplicationCaseData().ccdCaseReference(2801090368574910L)
            .generalAppPBADetails(new GeneralApplicationPbaDetails().setServiceReqReference("2023-1701090705688")
                                       .setFee(new Fee().setCalculatedAmountInPence(new BigDecimal("23200")))
                                       )
            .parentCaseReference("1701090368574910")
            .build();
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
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
            "2801090368574910",
            BEARER_TOKEN
        );
        assertThat(govPaymentRequest).isEqualTo(CardPaymentStatusResponse.from(response));

    }

    @Test
    @SneakyThrows
    void shouldCreateGovPayPaymentUrlForServiceRequestAdditionalPayment() {
        GeneralApplicationPbaDetails updatedPbaDetails = caseData.getGeneralAppPBADetails().copy()
            .setAdditionalPaymentServiceRef("2023-1701090705600");
        caseData = caseData.copy()
            .generalAppPBADetails(updatedPbaDetails)
            .build();
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
        CardPaymentServiceRequestResponse response = buildServiceRequestResponse();

        when(paymentsClient.createGovPayCardPaymentRequest(
            "2023-1701090705600",
            BEARER_TOKEN,
            CARD_PAYMENT_SERVICE_REQUEST
        )).thenReturn(response);

        CardPaymentStatusResponse govPaymentRequest = feesPaymentService.createGovPaymentRequest(
            "2801090368574910",
            BEARER_TOKEN
        );
        assertThat(govPaymentRequest).isEqualTo(CardPaymentStatusResponse.from(response));

    }

    @Test
    @SneakyThrows
    void shouldNotCreateGovPayPaymentUrlForMissingPbaDetails() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().ccdCaseReference(1701090368574910L)
                .generalAppPBADetails(new GeneralApplicationPbaDetails()
                        .setFee(new Fee().setCalculatedAmountInPence(new BigDecimal("23200")))
                        )
            .build();

        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
        assertThatThrownBy(
            () -> feesPaymentService.createGovPaymentRequest(
                "2801090368574910",
                BEARER_TOKEN
            )
        ).isInstanceOf(NullPointerException.class)
            .hasMessage("Fee Payment service request cannot be null");

        verify(paymentsClient, never()).createGovPayCardPaymentRequest(anyString(), anyString(), any());
    }

    @Test
    @SneakyThrows
    void shouldNotCreateGovPayPaymentUrlForMissingServiceRequest() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().ccdCaseReference(1701090368574910L)
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setFee(new Fee().setCalculatedAmountInPence(new BigDecimal("23200")))
                                      )
            .build();

        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);

        assertThatThrownBy(
            () -> feesPaymentService.createGovPaymentRequest(
                "2801090368574910",
                BEARER_TOKEN
            )
        ).isInstanceOf(NullPointerException.class)
            .hasMessage("Fee Payment service request cannot be null");

        verify(paymentsClient, never()).createGovPayCardPaymentRequest(anyString(), anyString(), any());
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
            () -> feesPaymentService.createGovPaymentRequest(
                "2801090368574910",
                BEARER_TOKEN
            )
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
            () -> feesPaymentService.createGovPaymentRequest(
                "2801090368574910",
                BEARER_TOKEN
            )
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
            feesPaymentService.createGovPaymentRequest(
                "2801090368574910",
                BEARER_TOKEN
            );

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

            "123",
            "RC-1701-0909-0602-0418",
            BEARER_TOKEN
        );

        assertThat(govPaymentRequestStatus).isEqualTo(expectedResponse(status));
        verify(updatePaymentStatusService, times(1)).updatePaymentStatus(any(), any());
    }

    @Test
    void shouldRetryPaymentsApiWhenInternalServerErrorThrown() {
        when(paymentsClient.getGovPayCardPaymentStatus("RC-1701-0909-0602-0418", BEARER_TOKEN))
            .thenThrow(FeignException.InternalServerError.class);

        assertThrows(
            PaymentsApiException.class,
            () -> feesPaymentService.getGovPaymentRequestStatus("123", "RC-1701-0909-0602-0418", BEARER_TOKEN)
        );

        verify(paymentsClient, times(5)).getGovPayCardPaymentStatus("RC-1701-0909-0602-0418", BEARER_TOKEN);
    }

    @Test
    void shouldNotRetryPaymentsApiWhenExceptionOtherThanInternalServerIsThrown() {
        when(paymentsClient.getGovPayCardPaymentStatus("RC-1701-0909-0602-0418", BEARER_TOKEN))
            .thenThrow(FeignException.NotImplemented.class);

        assertThrows(
            PaymentsApiException.class,
            () -> feesPaymentService.getGovPaymentRequestStatus("123", "RC-1701-0909-0602-0418", BEARER_TOKEN
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

        feesPaymentService.getGovPaymentRequestStatus("123", "RC-1701-0909-0602-0418", BEARER_TOKEN);

        verify(paymentsClient, times(3))
            .getGovPayCardPaymentStatus("RC-1701-0909-0602-0418", BEARER_TOKEN);
    }

    private PaymentDto buildGovPayCardPaymentStatusResponse(String status) {
        return PaymentDto.builder()
            .externalReference("lbh2ogknloh9p3b4lchngdfg63")
            .reference("RC-1701-0909-0602-0418")
            .status(status)
            .amount(new BigDecimal(200))
            .currency("GBP")
            .statusHistories(getStatusHistories(status))
            .build();
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

    private CardPaymentStatusResponse expectedResponse(String status) {
        CardPaymentStatusResponse payment = new CardPaymentStatusResponse()
            .setPaymentReference("RC-1701-0909-0602-0418")
            .setStatus(status)
            .setPaymentAmount(new BigDecimal(200))
            .setStatus(status);

        if (status.equals("Failed")) {
            payment.setErrorCode("P0030")
                .setErrorDescription("Payment was cancelled by the user");
        }

        return payment;
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
