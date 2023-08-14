package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.client.models.StatusHistoryDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAKE_PBA_PAYMENT_SPEC;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;

@SpringBootTest(classes = {
    PaymentsForSpecCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class PaymentsForSpecHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private PaymentsForSpecCallbackHandler handler;

    @MockBean
    private PaymentsService paymentsService;
    @MockBean
    private Time time;
    @Autowired
    private ObjectMapper objectMapper;
    private CallbackParams params;
    private CaseData caseData;
    private static final String PAYMENT_ERROR_MESSAGE = "Your account is deleted";
    private static final String PAYMENT_ERROR_CODE = "CA-E0004";

    @Test
    void shouldReturnCorrectActivityId_whenRequested() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        assertThat(handler.camundaActivityId(params)).isEqualTo("CreateClaimMakePaymentForSpec");
    }

    @Nested
    class AboutToSubmitTest {

        @BeforeEach
        public void setup() {
            caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            when(time.now()).thenReturn(LocalDateTime.of(2020, 1, 1, 12, 0, 0));
        }

        @Test
        void testAboutToSubmit_handler() {
            PaymentDto paymentDto = PaymentDto.builder().paymentReference("123").build();
            when(paymentsService.createCreditAccountPayment(any(), anyString())).thenReturn(paymentDto);
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("claimIssuedPaymentDetails")
                .extracting("reference", "status")
                .containsExactly("123", SUCCESS.toString());
            assertThat(response.getData()).containsEntry("paymentSuccessfulDate", "2020-01-01T12:00:00");
        }

        @Test
        void testAboutToSubmit_handlerWithError() {
            when(paymentsService.createCreditAccountPayment(any(), anyString())).thenThrow(buildFeignExceptionWithInvalidResponse(403, "Forbidden"));
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            assertThrows(FeignException.class, () -> handler.handle(params));
        }

        @Test
        void testAboutToSubmit_handlerWith400Error() throws JsonProcessingException {
            when(paymentsService.createCreditAccountPayment(any(), anyString())).thenThrow((buildFeignException(400)));
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).doesNotHaveToString("paymentReference");
            assertThat(response.getData()).extracting("paymentDetails")
                .extracting("status").isEqualTo(FAILED.toString());
        }

        @Test
        void testAboutToSubmit_handlerWith500Error() throws JsonProcessingException {
            when(paymentsService.createCreditAccountPayment(any(), anyString())).thenThrow((buildFeignExceptionWithInvalidResponse(500, "Internal server error")));
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).doesNotHaveToString("paymentReference");
            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains("Technical error occurred");
        }

        private FeignException buildFeignException(int status) throws JsonProcessingException {
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

        private FeignException buildFeignExceptionWithInvalidResponse(int status, String errorMsg) {
            return buildFeignClientException(status, errorMsg.getBytes(UTF_8));
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
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(MAKE_PBA_PAYMENT_SPEC);
    }
}
