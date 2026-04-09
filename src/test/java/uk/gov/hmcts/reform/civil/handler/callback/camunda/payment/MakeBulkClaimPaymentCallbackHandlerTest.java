package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import feign.FeignException;
import feign.Request;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SRPbaDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.payments.client.InvalidPaymentRequestException;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.client.models.StatusHistoryDto;
import uk.gov.hmcts.reform.payments.response.PBAServiceRequestResponse;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAKE_BULK_CLAIM_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;

@ExtendWith(MockitoExtension.class)
class MakeBulkClaimPaymentCallbackHandlerTest extends BaseCallbackHandlerTest {

    private static final String SUCCESSFUL_PAYMENT_REFERENCE = "RC-1234-1234-1234-1234";
    private static final String PAYMENT_ERROR_MESSAGE = "Your account is deleted";
    private static final String PAYMENT_ERROR_CODE = "CA-E0004";
    public static final String DUPLICATE_PAYMENT_MESSAGE
        = "You attempted to retry the payment too soon. Try again later.";

    @Mock
    private PaymentsService paymentsService;

    @Mock
    private Time time;

    private MakeBulkClaimPaymentCallbackHandler handler;
    private ObjectMapper objectMapper;
    private CaseData caseData;
    private CallbackParams params;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        handler = new MakeBulkClaimPaymentCallbackHandler(paymentsService, objectMapper, time);

        caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        caseData.setHearingDate(null);
        caseData.setSdtRequestIdFromSdt("testRequestId");
        caseData.setClaimIssuedPBADetails(new SRPbaDetails().setServiceReqReference("12345"));
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
    }

    @Test
    void shouldMakeBulkClaimPbaPayment_whenInvokedAndIsBulkClaim() {
        when(time.now()).thenReturn(LocalDateTime.of(1990, 2, 20, 12, 0, 0));

        when(paymentsService.createPbaPayment(any(), any()))
            .thenReturn(PBAServiceRequestResponse.builder().paymentReference(SUCCESSFUL_PAYMENT_REFERENCE).build());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(paymentsService).createPbaPayment(caseData, "BEARER_TOKEN");

        assertThat(response.getData()).extracting("claimIssuedPaymentDetails")
            .extracting("reference", "status", "customerReference")
            .containsExactly(SUCCESSFUL_PAYMENT_REFERENCE, SUCCESS.toString(), "12345");
        assertThat(response.getData()).containsEntry("paymentSuccessfulDate", "1990-02-20T12:00:00");
    }

    @Test
    void shouldNotMakeBulkClaimPbaPayment_whenInvokedAndIsNotBulkClaim() {
        verifyNoInteractions(paymentsService);
    }

    @ParameterizedTest
    @ValueSource(ints = {403, 422, 504})
    void shouldUpdateFailureReason_whenSpecificFeignExceptionsThrown(int status) {
        doThrow(buildFeignException(status)).when(paymentsService).createPbaPayment(any(), any());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(paymentsService).createPbaPayment(caseData, "BEARER_TOKEN");
        assertThat(response.getData()).extracting("claimIssuedPaymentDetails")
            .doesNotHaveToString("reference");
        assertThat(response.getData()).extracting("claimIssuedPaymentDetails")
            .extracting("errorMessage", "errorCode", "status", "customerReference")
            .containsExactly(PAYMENT_ERROR_MESSAGE, PAYMENT_ERROR_CODE, FAILED.toString(), "12345");
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 404, 409})
    void shouldAddError_whenOtherExceptionThrown(int status) {
        doThrow(buildFeignException(status)).when(paymentsService).createPbaPayment(any(), any());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(paymentsService).createPbaPayment(caseData, "BEARER_TOKEN");
        assertThat(response.getData()).doesNotHaveToString("paymentReference");
        assertThat(response.getData()).extracting("claimIssuedPaymentDetails")
            .doesNotHaveToString("paymentErrorMessage");
        assertThat(response.getData()).extracting("claimIssuedPaymentDetails")
            .doesNotHaveToString("paymentErrorCode");
        assertThat(response.getErrors()).containsOnly("Technical error occurred");
    }

    @Test
    void shouldNotThrowError_whenPaymentIsResubmittedWithInTwoMinutes() {
        doThrow(new InvalidPaymentRequestException("Duplicate Payment."))
            .when(paymentsService).createPbaPayment(any(), any());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(paymentsService).createPbaPayment(caseData, "BEARER_TOKEN");
        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getData()).extracting("claimIssuedPaymentDetails")
            .extracting("errorMessage", "status", "customerReference")
            .containsExactly(DUPLICATE_PAYMENT_MESSAGE, FAILED.toString(), "12345");
    }

    @SneakyThrows
    private FeignException buildFeignException(int status) {
        return buildFeignClientException(status, objectMapper.writeValueAsBytes(
            PaymentDto.builder()
                .statusHistories(new StatusHistoryDto[]{
                    StatusHistoryDto.builder()
                        .errorCode(PAYMENT_ERROR_CODE)
                        .errorMessage(PAYMENT_ERROR_MESSAGE)
                        .build()
                })
                .build()
        ));
    }

    private FeignException.FeignClientException buildFeignClientException(int status, byte[] body) {
        return new FeignException.FeignClientException(
            status,
            "exception message",
            Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null),
            body,
            Collections.emptyMap()
        );
    }

    @Test
    void shouldThrowException_whenForbiddenExceptionThrownContainsInvalidResponse() {
        doThrow(buildForbiddenFeignExceptionWithInvalidResponse())
            .when(paymentsService).createPbaPayment(any(), any());

        assertThrows(FeignException.class, () -> handler.handle(params));
        verify(paymentsService).createPbaPayment(caseData, "BEARER_TOKEN");
    }

    private FeignException buildForbiddenFeignExceptionWithInvalidResponse() {
        return buildFeignClientException(403, "unexpected response body".getBytes(UTF_8));
    }

    @Test
    void shouldReturnCorrectActivityIdAndCorrectEvent_whenInvoked() {
        CallbackParams localParams = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        assertThat(handler.camundaActivityId(localParams)).isEqualTo("makeBulkClaimPayment");
        assertThat(handler.handledEvents()).contains(MAKE_BULK_CLAIM_PAYMENT);
    }
}
