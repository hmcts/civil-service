package uk.gov.hmcts.reform.civil.controllers.fees;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.civil.service.PaymentRequestUpdateCallbackService;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ServiceRequestUpdateClaimIssuedCallbackControllerTest extends BaseIntegrationTest {

    private static final String PAYMENT_CALLBACK_URL = "/service-request-update-claim-issued";
    private static final String CCD_CASE_NUMBER = "1234";
    private static final String PAID = "Paid";
    private static final String REFERENCE = "reference";
    private static final String ACCOUNT_NUMBER = "123445555";
    @MockBean
    CoreCaseDataApi coreCaseDataApi;

    @MockBean
    AuthTokenGenerator authTokenGenerator;

    @SpyBean
    PaymentRequestUpdateCallbackService requestUpdateCallbackService;

    @BeforeEach
    void bareMinimumToMakeAPositiveRequest() {
        when(authorisationService.isServiceAuthorized(any())).thenReturn(true);
        CaseData
            caseData = CaseData.builder().businessProcess(new BusinessProcess().setProcessInstanceId("instance").setCamundaEvent("camunda event")).build();
        CaseDetails caseDetails = CaseDetails.builder().build();
        caseDetails.setData(caseData.toMap(objectMapper));
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

        given(authTokenGenerator.generate()).willReturn("some arbitrary token");
        given(coreCaseDataApi.getCase(any(), any(), any())).willReturn(caseDetails);
        given(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(), any(), any(), any(), any())).willReturn(startEventResponse);
        given(coreCaseDataApi.submitEventForCaseWorker(any(), any(), any(), any(), any(), any(), anyBoolean(), any())).willReturn(caseDetails);
    }

    @Test
    public void whenInvalidTypeOfRequestMade_ReturnMethodNotAllowed() throws Exception {

        doPost(buildServiceDto(), PAYMENT_CALLBACK_URL, "")
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void whenServiceRequestUpdateRequestAndEverythingIsOk_thenHttp2xx() throws Exception {

        doPut(buildServiceDto(), PAYMENT_CALLBACK_URL, "")
            .andExpect(status().isOk());
    }

    @Test
    public void whenServiceRequestUpdateRequestButUnexpectedErrorOccurs_thenHttp5xx() throws Exception {
        // Given: the callback processing throws an unexpected exception
        doThrow(new RuntimeException("Unexpected error"))
            .when(requestUpdateCallbackService)
            .processCallback(any(), any());

        // When: I call the /service-request-update URL
        doPut(buildServiceDto(), PAYMENT_CALLBACK_URL, "")
            // Then: the result status must be an HTTP-5xx
            .andExpect(status().is5xxServerError());
    }

    @Test
    public void whenValidPaymentCallbackIsReceivedReturnSuccess() throws Exception {
        doPut(buildServiceDto(), PAYMENT_CALLBACK_URL, "")
            .andExpect(status().isOk());
    }

    @Test
    public void whenPaymentCallbackIsReceivedWithoutServiceAuthorisationReturn400() throws Exception {
        mockMvc.perform(
            MockMvcRequestBuilders.put(PAYMENT_CALLBACK_URL, "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buildServiceDto()))).andExpect(status().is4xxClientError());
    }

    @Test
    public void whenPaymentCallbackIsReceivedWithServiceAuthorisationButreturnsfalseReturn400() throws Exception {
        when(authorisationService.isServiceAuthorized(any())).thenReturn(false);

        doPut(buildServiceDto(), PAYMENT_CALLBACK_URL, "")
            // Then: the result status must be an HTTP-4xx
            .andExpect(status().is5xxServerError());

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
                .header("ServiceAuthorization", "s2s AuthToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(content)));
    }

    @SneakyThrows
    protected <T> ResultActions doPost(T content, String urlTemplate, Object... uriVars) {
        return mockMvc.perform(
            MockMvcRequestBuilders.post(urlTemplate, uriVars)
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                .header("ServiceAuthorization", "s2s AuthToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(content)));
    }
}
