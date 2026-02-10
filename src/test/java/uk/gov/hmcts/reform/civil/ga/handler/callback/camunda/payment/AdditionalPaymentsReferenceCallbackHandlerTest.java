package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.JudicialDecisionHelper;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.payments.client.InvalidPaymentRequestException;
import uk.gov.hmcts.reform.payments.response.PaymentServiceResponse;

import java.time.LocalDateTime;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.OBTAIN_ADDITIONAL_PAYMENT_REF;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class AdditionalPaymentsReferenceCallbackHandlerTest  extends GeneralApplicationBaseCallbackHandlerTest {

    private static final String PAYMENT_REQUEST_REFERENCE = "RC-1234-1234-1234-1234";
    public static final String BEARER_TOKEN = "BEARER_TOKEN";
    public static final String DUPLICATE_PAYMENT = "Duplicate Payment.";
    public static final String UNEXPECTED_RESPONSE_BODY = "unexpected response body";
    public static final String EXCEPTION_MESSAGE = "exception message";

    @Mock
    private Time time;

    @InjectMocks
    private AdditionalPaymentsReferenceCallbackHandler handler;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private CallbackParams params;
    @Mock
    private PaymentsService paymentsService;

    @Mock
    JudicialDecisionHelper judicialDecisionHelper;

    @BeforeEach
    public void setup() {
        when(time.now()).thenReturn(LocalDateTime.of(2020, 1, 1, 12, 0, 0));
    }

    @Nested
    class MakeAdditionalPaymentReference {

        @BeforeEach
        void setup() {

            when(paymentsService.createServiceRequestGa(any(), any()))
                .thenReturn(PaymentServiceResponse.builder().serviceRequestReference(PAYMENT_REQUEST_REFERENCE)
                                .build());
        }

        @Test
        void shouldMakeAdditionalPaymentReference_whenJudgeUncloakedApplication()  {
            var caseData = GeneralApplicationCaseDataBuilder.builder()
                .judicialDecisionWithUncloakRequestForInformationApplication(
                    REQUEST_MORE_INFORMATION, YesOrNo.NO, YesOrNo.NO)
                .build();
            when(judicialDecisionHelper
                     .isApplicationUncloakedWithAdditionalFee(caseData)).thenReturn(true);

            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService).createServiceRequestGa(caseData, BEARER_TOKEN);
            assertThat(extractPaymentRequestReferenceFromResponse(response))
                .isEqualTo(PAYMENT_REQUEST_REFERENCE);
        }

        @Test
        void shouldNotMakeAdditionalPaymentReference_whenJudgeNotUncloakedApplication() {
            var caseData = GeneralApplicationCaseDataBuilder.builder().requestForInformationApplication()
                .build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(judicialDecisionHelper
                     .isApplicationUncloakedWithAdditionalFee(caseData)).thenReturn(false);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService, never()).createServiceRequestGa(any(), any());
            assertThat(extractPaymentRequestReferenceFromResponse(response))
                .isNull();
        }

        @Test
        void shouldNotMakeAdditionalPaymentRef_whenJudgeNotUncloakedApplication_OtherThanRequestMoreInformation() {
            var caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.NO)
                .build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService, never()).createServiceRequest(any(), any());
            assertThat(extractPaymentRequestReferenceFromResponse(response))
                .isNull();
        }

        @Test
        void shouldThrowException_whenForbiddenExceptionThrownContainsInvalidResponse() {
            doThrow(buildForbiddenFeignExceptionWithInvalidResponse())
                .when(paymentsService).createServiceRequestGa(any(), any());
            var caseData = GeneralApplicationCaseDataBuilder.builder()
                .judicialDecisionWithUncloakRequestForInformationApplication(REQUEST_MORE_INFORMATION,
                                                                             YesOrNo.NO, YesOrNo.NO)
                .build();
            when(judicialDecisionHelper
                     .isApplicationUncloakedWithAdditionalFee(caseData)).thenReturn(true);
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            assertThrows(FeignException.class, () -> handler.handle(params));
            verify(paymentsService).createServiceRequestGa(caseData, BEARER_TOKEN);
        }

        @Test
        void shouldNotThrowError_whenPaymentIsResubmittedWithInTwoMinutes() {
            doThrow(new InvalidPaymentRequestException(DUPLICATE_PAYMENT))
                .when(paymentsService).createServiceRequestGa(any(), any());

            var caseData = GeneralApplicationCaseDataBuilder.builder()
                .judicialDecisionWithUncloakRequestForInformationApplication(
                    REQUEST_MORE_INFORMATION, YesOrNo.NO, YesOrNo.NO)
                .build();

            when(judicialDecisionHelper
                     .isApplicationUncloakedWithAdditionalFee(caseData)).thenReturn(true);
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService).createServiceRequestGa(caseData, BEARER_TOKEN);

            assertThat(extractPaymentRequestReferenceFromResponse(response)).isNull();
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnCorrectActivityId_whenRequested() {
            assertThat(handler.camundaActivityId(CallbackParams.builder().build())).isEqualTo("GeneralApplicationMakeAdditionalPayment");
        }

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents()).contains(OBTAIN_ADDITIONAL_PAYMENT_REF);
        }

    }

    private String extractPaymentRequestReferenceFromResponse(AboutToStartOrSubmitCallbackResponse response) {
        GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        return responseCaseData.getGeneralAppPBADetails().getAdditionalPaymentServiceRef();
    }

    private FeignException buildForbiddenFeignExceptionWithInvalidResponse() {
        return buildFeignClientException(403, UNEXPECTED_RESPONSE_BODY.getBytes(UTF_8));
    }

    private FeignException.FeignClientException buildFeignClientException(int status, byte[] body) {
        return new FeignException.FeignClientException(
            status,
            EXCEPTION_MESSAGE,
            Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null),
            body,
            Map.of()
        );
    }

}
