package uk.gov.hmcts.reform.unspec.handler.callback.camunda.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.client.models.StatusHistoryDto;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.service.PaymentsService;

import java.util.HashMap;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;

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

    @Autowired
    private PaymentsCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    private CaseData caseData;
    private CallbackParams params;

    @BeforeEach
    public void setup() {
        caseData = CaseDataBuilder.builder().atStatePendingCaseIssued().build();
        params = callbackParamsOf(new HashMap<>(), ABOUT_TO_SUBMIT)
            .toBuilder().caseData(caseData).build();
    }

    @Test
    void shouldMakePbaPayment_whenInvoked() {
        when(paymentsService.createCreditAccountPayment(any(), any()))
            .thenReturn(PaymentDto.builder().reference(SUCCESSFUL_PAYMENT_REFERENCE).build());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(paymentsService).createCreditAccountPayment(caseData, "BEARER_TOKEN");
        assertThat(response.getData()).extracting("paymentDetails").extracting("reference")
            .isEqualTo(SUCCESSFUL_PAYMENT_REFERENCE);
    }

    @ParameterizedTest
    @ValueSource(ints = {403})
    void shouldUpdateFailureReason_whenForbiddenExceptionThrown(int status) {
        doThrow(buildFeignException(status)).when(paymentsService).createCreditAccountPayment(any(), any());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(paymentsService).createCreditAccountPayment(caseData, "BEARER_TOKEN");
        assertThat(response.getData()).extracting("paymentDetails").extracting("reference").isNull();
        assertThat(response.getData()).extracting("paymentDetails").extracting("errorMessage")
            .isEqualTo(PAYMENT_ERROR_MESSAGE);
        assertThat(response.getData()).extracting("paymentDetails").extracting("errorCode")
            .isEqualTo(PAYMENT_ERROR_CODE);
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 404, 422, 504})
    void shouldAddError_whenOtherExceptionThrown(int status) {
        doThrow(buildFeignException(status)).when(paymentsService).createCreditAccountPayment(any(), any());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(paymentsService).createCreditAccountPayment(caseData, "BEARER_TOKEN");
        assertThat(response.getData()).extracting("paymentReference").isNull();
        assertThat(response.getData()).extracting("paymentErrorMessage").isNull();
        assertThat(response.getData()).extracting("paymentErrorCode").isNull();
        assertThat(response.getErrors()).containsOnly("Technical error occurred");
    }

    @Test
    void shouldThrowException_whenForbiddenExceptionThrownContainsInvalidResponse() {
        doThrow(buildForbiddenFeignExceptionWithInvalidResponse())
            .when(paymentsService).createCreditAccountPayment(any(), any());

        assertThrows(FeignException.class, () -> handler.handle(params));
        verify(paymentsService).createCreditAccountPayment(caseData, "BEARER_TOKEN");
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

    private FeignException buildForbiddenFeignExceptionWithInvalidResponse() {
        return buildFeignClientException(403, "unexpected response body".getBytes(UTF_8));
    }

    private FeignException.FeignClientException buildFeignClientException(int status, byte[] body) {
        return new FeignException.FeignClientException(
            status,
            "exception message",
            Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null),
            body
        );
    }
}
