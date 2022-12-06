package uk.gov.hmcts.reform.civil.controllers.fees;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ServiceRequestUpdateCallbackControllerTest extends BaseIntegrationTest {

    private static final String PAYMENT_CALLBACK_URL = "/service-request-update";
    private static final String CCD_CASE_NUMBER = "1234";
    private static final String PAID = "Paid";
    private static final String REFERENCE = "reference";
    private static final String ACCOUNT_NUMBER = "123445555";

    @Test
    public void whenInvalidTypeOfRequestMade_ReturnMethodNotAllowed() throws Exception {

        doPost(buildServiceDto(), PAYMENT_CALLBACK_URL, "")
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void whenServiceRequestUpdateRequest() throws Exception {

        doPut(buildServiceDto(), PAYMENT_CALLBACK_URL, "")
            .andExpect(status().isOk());
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
        return mockMvc.perform(
            MockMvcRequestBuilders.put(urlTemplate, uriVars)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(content)));
    }

    @SneakyThrows
    protected <T> ResultActions doPost(T content, String urlTemplate, Object... uriVars) {
        return mockMvc.perform(
            MockMvcRequestBuilders.post(urlTemplate, uriVars)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(content)));
    }
}
