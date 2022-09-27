package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.client.models.StatusHistoryDto;

import java.time.LocalDateTime;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;

@SpringBootTest(classes = {
    PaymentsCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class PaymentsCallbackHandlerTest extends BaseCallbackHandlerTest {

    private static final String SUCCESSFUL_PAYMENT_REFERENCE = "RC-1234-1234-1234-1234";
    private static final String PAYMENT_ERROR_MESSAGE = "Your account is deleted";
    private static final String PAYMENT_ERROR_CODE = "CA-E0004";

    @MockBean
    private PaymentsService paymentsService;

    @MockBean
    private Time time;

    @Autowired
    private PaymentsCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    private CaseData caseData;
    private CallbackParams params;

    @BeforeEach
    public void setup() {
        caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();

        when(time.now()).thenReturn(LocalDateTime.of(2020, 1, 1, 12, 0, 0));
    }

    @Nested
    class NewCode {

        @BeforeEach
        void setup() {
            params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);
        }

        @Test
        void shouldMakePbaPayment_whenInvoked() {
            when(paymentsService.createCreditAccountPayment(any(), any()))
                .thenReturn(PaymentDto.builder().reference(SUCCESSFUL_PAYMENT_REFERENCE).build());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService).createCreditAccountPayment(caseData, "BEARER_TOKEN");
            assertThat(response.getData()).extracting("claimIssuedPaymentDetails")
                .extracting("reference", "status", "customerReference")
                .containsExactly(SUCCESSFUL_PAYMENT_REFERENCE, SUCCESS.toString(), "12345");
            assertThat(response.getData()).containsEntry("paymentSuccessfulDate", "2020-01-01T12:00:00");
        }

        @ParameterizedTest
        @ValueSource(ints = {403})
        void shouldUpdateFailureReason_whenForbiddenExceptionThrown(int status) {
            doThrow(buildFeignException(status)).when(paymentsService).createCreditAccountPayment(any(), any());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService).createCreditAccountPayment(caseData, "BEARER_TOKEN");
            assertThat(response.getData()).extracting("claimIssuedPaymentDetails").extracting("reference").isNull();
            assertThat(response.getData()).extracting("claimIssuedPaymentDetails")
                .extracting("errorMessage", "errorCode", "status", "customerReference")
                .containsExactly(PAYMENT_ERROR_MESSAGE, PAYMENT_ERROR_CODE, FAILED.toString(), "12345");
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotThrowError_whenPaymentIsResubmittedWithInTwoMinutes() {
            doThrow(buildFeignException(400)).when(paymentsService).createCreditAccountPayment(any(), any());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService).createCreditAccountPayment(caseData, "BEARER_TOKEN");
            assertThat(response.getErrors()).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(ints = {404, 422, 504})
        void shouldAddError_whenOtherExceptionThrown(int status) {
            doThrow(buildFeignException(status)).when(paymentsService).createCreditAccountPayment(any(), any());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService).createCreditAccountPayment(caseData, "BEARER_TOKEN");
            assertThat(response.getData()).extracting("paymentReference").isNull();
            assertThat(response.getData()).extracting("claimIssuedPaymentDetails")
                .extracting("paymentErrorMessage", "paymentErrorCode")
                .containsExactly(null, null);
            assertThat(response.getErrors()).containsOnly("Technical error occurred");
        }

        @Test
        void shouldThrowException_whenForbiddenExceptionThrownContainsInvalidResponse() {
            doThrow(buildForbiddenFeignExceptionWithInvalidResponse())
                .when(paymentsService).createCreditAccountPayment(any(), any());

            assertThrows(FeignException.class, () -> handler.handle(params));
            verify(paymentsService).createCreditAccountPayment(caseData, "BEARER_TOKEN");
        }
    }

    @Nested
    class OldCode {

        @BeforeEach
        void setup() {
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        }

        @Test
        void shouldMakePbaPayment_whenInvoked() {
            when(paymentsService.createCreditAccountPayment(any(), any()))
                .thenReturn(PaymentDto.builder().reference(SUCCESSFUL_PAYMENT_REFERENCE).build());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService).createCreditAccountPayment(caseData, "BEARER_TOKEN");
            assertThat(response.getData()).extracting("paymentDetails")
                .extracting("reference", "status", "customerReference")
                .containsExactly(SUCCESSFUL_PAYMENT_REFERENCE, SUCCESS.toString(), null);
            assertThat(response.getData()).containsEntry("paymentSuccessfulDate", "2020-01-01T12:00:00");
        }

        @ParameterizedTest
        @ValueSource(ints = {403})
        void shouldUpdateFailureReason_whenForbiddenExceptionThrown(int status) {
            doThrow(buildFeignException(status)).when(paymentsService).createCreditAccountPayment(any(), any());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService).createCreditAccountPayment(caseData, "BEARER_TOKEN");
            assertThat(response.getData()).extracting("paymentDetails").extracting("reference").isNull();
            assertThat(response.getData()).extracting("paymentDetails")
                .extracting("errorMessage", "errorCode", "status", "customerReference")
                .containsExactly(PAYMENT_ERROR_MESSAGE, PAYMENT_ERROR_CODE, FAILED.toString(), null);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotThrowError_whenPaymentIsResubmittedWithInTwoMinutes() {
            doThrow(buildFeignException(400)).when(paymentsService).createCreditAccountPayment(any(), any());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService).createCreditAccountPayment(caseData, "BEARER_TOKEN");
            assertThat(response.getErrors()).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(ints = {404, 422, 504})
        void shouldAddError_whenOtherExceptionThrown(int status) {
            doThrow(buildFeignException(status)).when(paymentsService).createCreditAccountPayment(any(), any());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService).createCreditAccountPayment(caseData, "BEARER_TOKEN");
            assertThat(response.getData()).extracting("paymentReference").isNull();
            assertThat(response.getData()).extracting("paymentDetails").isNull();
            assertThat(response.getErrors()).containsOnly("Technical error occurred");
        }

        @Test
        void shouldThrowException_whenForbiddenExceptionThrownContainsInvalidResponse() {
            doThrow(buildForbiddenFeignExceptionWithInvalidResponse())
                .when(paymentsService).createCreditAccountPayment(any(), any());

            assertThrows(FeignException.class, () -> handler.handle(params));
            verify(paymentsService).createCreditAccountPayment(caseData, "BEARER_TOKEN");
        }
    }

    @SneakyThrows
    private FeignException buildFeignException(int status) {
        return buildFeignClientException(status, objectMapper.writeValueAsBytes(
            PaymentDto.builder()
                .statusHistories(new StatusHistoryDto[] {
                    StatusHistoryDto.builder()
                        .errorCode(PAYMENT_ERROR_CODE)
                        .errorMessage(PAYMENT_ERROR_MESSAGE)
                        .build()
                })
                .build()
        ));
    }

    private FeignException buildForbiddenFeignExceptionWithInvalidResponse() {
        return buildFeignClientException(403, "unexpected response body".getBytes(UTF_8));
    }

    private FeignException.FeignClientException buildFeignClientException(int status, byte[] body) {
        return new FeignException.FeignClientException(
            status,
            "exception message",
            Request.create(GET, "", Map.of(), new byte[] {}, UTF_8, null),
            body
        );
    }
}
