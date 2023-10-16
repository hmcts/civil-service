package uk.gov.hmcts.reform.civil.handler.callback.hmc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UpdateNextHearingInfo;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;

@SpringBootTest(classes = {
    NextHearingDateSchedulerCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
public class NextHearingDateSchedulerCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private NextHearingDateSchedulerCallbackHandler handler;

    @MockBean
    FeatureToggleService featureToggleService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    class AboutToSubmit {

        @Test
        void shouldSetBusinessProcessToUpdateNextHearingInfo() {
            when(featureToggleService.isNextHearingDateEnabled()).thenReturn(true);

            CaseData caseData = CaseData.builder()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.FINISHED).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, UpdateNextHearingInfo, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertEquals(UpdateNextHearingInfo.name(), updatedData.getBusinessProcess().getCamundaEvent());
            assertEquals(READY, updatedData.getBusinessProcess().getStatus());
        }

        @Test
        void shouldNotUpdateCaseData_whenNextHearingDateIsNotEnabled() {
            when(featureToggleService.isNextHearingDateEnabled()).thenReturn(false);

            CaseData caseData = CaseData.builder()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.FINISHED).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, UpdateNextHearingInfo, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertNull(response.getData());
        }
    }
}
