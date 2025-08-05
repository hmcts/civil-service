package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
public class ClaimantFullAdmitPayImmediatelyCCJCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private ClaimantFullAdmitPayImmediatelyCCJCallbackHandler handler;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        handler = new ClaimantFullAdmitPayImmediatelyCCJCallbackHandler(objectMapper);
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldReturnFAImmediateCcjBusinessProcess_WhenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            var businessProcess = objectMapper.convertValue(response.getData().get("businessProcess"), BusinessProcess.class);

            assertThat(businessProcess.getStatus()).isEqualTo(BusinessProcessStatus.READY);
            assertThat(businessProcess.getCamundaEvent()).isEqualTo(CaseEvent.CLAIMANT_RESPONSE_FA_IMMEDIATE_CCJ.name());
        }
    }
}
