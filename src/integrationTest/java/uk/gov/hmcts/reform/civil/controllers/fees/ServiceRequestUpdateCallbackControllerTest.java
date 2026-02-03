package uk.gov.hmcts.reform.civil.controllers.fees;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
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
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ServiceRequestUpdateCallbackControllerTest extends BaseIntegrationTest {

    private static final String PAYMENT_CALLBACK_URL = "/service-request-update";
    private static final String CCD_CASE_NUMBER = "1234";
    private static final String PAID = "Paid";
    private static final String REFERENCE = "reference";
    private static final String ACCOUNT_NUMBER = "123445555";

    @MockBean
    CoreCaseDataApi coreCaseDataApi;

    @MockBean
    AuthTokenGenerator authTokenGenerator;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    void bareMinimumToMakeAPositiveRequest() {
        when(authorisationService.isServiceAuthorized(any())).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder()
                                 .processInstanceId("instance")
                                 .camundaEvent("camunda event")
                                 .build())
            .build();

        CaseDetails caseDetails = CaseDetails.builder().build();
        caseDetails.setData(caseData.toMap(objectMapper));

        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .build();

        given(authTokenGenerator.generate()).willReturn("some arbitrary token");
        given(coreCaseDataApi.getCase(any(), any(), any())).willReturn(caseDetails);
        given(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(), any(), any(), any(), any()))
            .willReturn(startEventResponse);
        given(coreCaseDataApi.submitEventForCaseWorker(any(), any(), any(), any(), any(), any(), anyBoolean(), any()))
            .willReturn(caseDetails);
    }

    @Test
    public void whenValidPaymentCallbackIsReceivedReturnSuccess() throws Exception {
        doPut(buildServiceDto(), PAYMENT_CALLBACK_URL, "")
            .andExpect(status().isOk());
    }

    @Test
    public void whenPaymentCallbackIsReceivedWithoutServiceAuthorisationReturn400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put(PAYMENT_CALLBACK_URL, "")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(buildServiceDto())))
            .andExpect(status().is4xxClientError());
    }

    @Test
    public void whenPaymentCallbackIsReceivedWithServiceAuthorisationButreturnsfalseReturn500() throws Exception {
        when(authorisationService.isServiceAuthorized(any())).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.put(PAYMENT_CALLBACK_URL, "")
                            .header("ServiceAuthorization", s2sToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(buildServiceDto())))
            .andExpect(status().is5xxServerError());
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
        // Get the real service bean
        CoreCaseDataService realService = applicationContext.getBean(CoreCaseDataService.class);

        // Create a spy on it
        CoreCaseDataService spyService = spy(realService);

        doThrow(new RuntimeException("Simulated CCD failure"))
            .when(spyService).submitUpdate(any(), any());

        Object controller = applicationContext.getBean("serviceRequestUpdateCallbackController");
        ReflectionTestUtils.setField(controller, "coreCaseDataService", spyService);

        doPut(buildServiceDto(), PAYMENT_CALLBACK_URL, "")
            .andExpect(status().is5xxServerError());
    }

    private ServiceRequestUpdateDto buildServiceDto() {
        return ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(CCD_CASE_NUMBER)
            .serviceRequestStatus(PAID)
            .payment(PaymentDto.builder()
                         .amount(new BigDecimal(167))
                         .paymentReference(REFERENCE)
                         .caseReference(REFERENCE)
                         .accountNumber(ACCOUNT_NUMBER)
                         .build())
            .build();
    }

    @SneakyThrows
    protected <T> ResultActions doPut(T content, String urlTemplate, Object... uriVars) {
        return mockMvc.perform(MockMvcRequestBuilders.put(urlTemplate, uriVars)
                                   .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                                   .header("ServiceAuthorization", s2sToken)
                                   .contentType(MediaType.APPLICATION_JSON)
                                   .content(toJson(content)));
    }

    @SneakyThrows
    protected <T> ResultActions doPost(T content, String urlTemplate, Object... uriVars) {
        return mockMvc.perform(MockMvcRequestBuilders.post(urlTemplate, uriVars)
                                   .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                                   .header("ServiceAuthorization", s2sToken)
                                   .contentType(MediaType.APPLICATION_JSON)
                                   .content(toJson(content)));
    }
}
