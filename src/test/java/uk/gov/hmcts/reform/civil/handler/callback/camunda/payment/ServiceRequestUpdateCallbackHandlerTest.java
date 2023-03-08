package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SERVICE_REQUEST_RECEIVED;

@SpringBootTest(classes = {
    ServiceRequestUpdateCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
public class ServiceRequestUpdateCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private ServiceRequestUpdateCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    private CaseData caseData;
    private CallbackParams params;

    @Test
    void shouldChangeApplicationState_whenInvoked() {
        //Given: Case data with hearingFee PBA details
        caseData = CaseDataBuilder.builder()
            .buildMakePaymentsCaseDataWithHearingDueDateWithHearingFeePBADetails();
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        //when: handler is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        //Then: response should contain hearingFeePBA details
        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
        assertThat(caseData.getHearingFeePBADetails().getServiceReqReference()).isEqualTo(responseCaseData.getHearingFeePBADetails().getServiceReqReference());
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(SERVICE_REQUEST_RECEIVED);
    }

}
