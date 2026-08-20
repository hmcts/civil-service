package uk.gov.hmcts.reform.civil.controllers.fees;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.exceptions.InternalServerErrorException;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.civil.service.PaymentRequestUpdateCallbackService;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ServiceRequestUpdateCallbackControllerTest extends BaseIntegrationTest {

    private static final String PAYMENT_CALLBACK_URL = "/service-request-update";
    private static final String CCD_CASE_NUMBER = "1234";
    private static final String PAID = "Paid";
    private static final String REFERENCE = "reference";
    private static final String ACCOUNT_NUMBER = "123445555";
    private static final String S2S_AUTH_TOKEN = "s2s AuthToken";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @MockBean
    CoreCaseDataApi coreCaseDataApi;

    @MockBean
    AuthTokenGenerator authTokenGenerator;

    @SpyBean
    PaymentRequestUpdateCallbackService requestUpdateCallbackService;

    @Autowired
    ServiceRequestUpdateCallbackController controller;

    @BeforeEach
    void bareMinimumToMakeAPositiveRequest() {
        when(authorisationService.isPaymentCallbackServiceAuthorized(any())).thenReturn(true);
        CaseData caseData = CaseData.builder().businessProcess(new BusinessProcess().setProcessInstanceId("instance").setCamundaEvent("camunda event")).build();
        CaseDetails caseDetails = CaseDetails.builder().build();
        caseDetails.setData(caseData.toMap(objectMapper));
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

        given(authTokenGenerator.generate()).willReturn("some arbitrary token");
        given(coreCaseDataApi.getCase(any(), any(), any())).willReturn(caseDetails);
        given(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(), any(), any(), any(), any())).willReturn(startEventResponse);
        given(coreCaseDataApi.submitEventForCaseWorker(any(), any(), any(), any(), any(), any(), anyBoolean(), any())).willReturn(caseDetails);
    }

    @Test
    public void shouldProcessServiceRequestUpdateWhenAuthorised() {
        ServiceRequestUpdateDto request = buildServiceDto();

        assertThatCode(() -> controller.serviceRequestUpdate(S2S_AUTH_TOKEN, request))
            .doesNotThrowAnyException();

        verify(authorisationService).isPaymentCallbackServiceAuthorized(S2S_AUTH_TOKEN);
        verify(requestUpdateCallbackService).processCallback(request, FeeType.HEARING.name());
    }

    @Test
    public void whenPaymentCallbackIsReceivedWithoutServiceAuthorisationReturn400() throws Exception {
        mockMvc.perform(
            MockMvcRequestBuilders.put(PAYMENT_CALLBACK_URL, "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buildServiceDto()))).andExpect(status().is4xxClientError());
    }

    @Test
    public void shouldThrowInternalServerErrorWhenServiceIsNotAuthorised() {
        ServiceRequestUpdateDto request = buildServiceDto();
        when(authorisationService.isPaymentCallbackServiceAuthorized(any())).thenReturn(false);

        assertThatThrownBy(() -> controller.serviceRequestUpdate(S2S_AUTH_TOKEN, request))
            .isInstanceOf(InternalServerErrorException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .cause()
            .hasMessage("Invalid Client");

        verify(authorisationService).isPaymentCallbackServiceAuthorized(S2S_AUTH_TOKEN);
        verify(requestUpdateCallbackService, never()).processCallback(any(), any());
    }

    @Test
    public void shouldWrapExceptionThrownByAuthorisationService() {
        ServiceRequestUpdateDto request = buildServiceDto();
        RuntimeException exception = new RuntimeException("Authorisation service unavailable");
        doThrow(exception)
            .when(authorisationService).isPaymentCallbackServiceAuthorized(S2S_AUTH_TOKEN);

        assertThatThrownBy(() -> controller.serviceRequestUpdate(S2S_AUTH_TOKEN, request))
            .isInstanceOf(InternalServerErrorException.class)
            .hasCause(exception);

        verify(authorisationService).isPaymentCallbackServiceAuthorized(S2S_AUTH_TOKEN);
        verify(requestUpdateCallbackService, never()).processCallback(any(), any());
    }

    @Test
    public void whenInvalidTypeOfRequestMadeThenReturnMethodNotAllowed() throws Exception {

        doPost(buildServiceDto(), PAYMENT_CALLBACK_URL, "")
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void shouldWrapExceptionThrownWhileProcessingServiceRequestUpdate() {
        ServiceRequestUpdateDto request = buildServiceDto();
        RuntimeException exception = new RuntimeException("Unexpected error");
        doThrow(exception)
            .when(requestUpdateCallbackService)
            .processCallback(request, FeeType.HEARING.name());

        assertThatThrownBy(() -> controller.serviceRequestUpdate(S2S_AUTH_TOKEN, request))
            .isInstanceOf(InternalServerErrorException.class)
            .hasCause(exception);

        verify(authorisationService).isPaymentCallbackServiceAuthorized(S2S_AUTH_TOKEN);
        verify(requestUpdateCallbackService).processCallback(request, FeeType.HEARING.name());
    }

    private ServiceRequestUpdateDto buildServiceDto() {
        return new ServiceRequestUpdateDto()
            .setCcdCaseNumber(CCD_CASE_NUMBER)
            .setServiceRequestStatus(PAID)
            .setPayment(PaymentDto.builder()
                .amount(new BigDecimal(167))
                .paymentReference(REFERENCE)
                .caseReference(REFERENCE)
                .accountNumber(ACCOUNT_NUMBER)
                .build());
    }

    @SneakyThrows
    protected <T> ResultActions doPut(T content, String urlTemplate, Object... uriVars) {
        return mockMvc.perform(
            MockMvcRequestBuilders.put(urlTemplate, uriVars)
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                .header(SERVICE_AUTHORIZATION, S2S_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(content)));
    }

    @SneakyThrows
    protected <T> ResultActions doPost(T content, String urlTemplate, Object... uriVars) {
        return mockMvc.perform(
            MockMvcRequestBuilders.post(urlTemplate, uriVars)
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                .header(SERVICE_AUTHORIZATION, S2S_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(content)));
    }
}
