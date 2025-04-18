package uk.gov.hmcts.reform.civil.handler.callback.camunda.businessprocess;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.DISPATCHED;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;

@ExtendWith(MockitoExtension.class)
class DispatchBusinessProcessCallbackHandlerTest extends BaseCallbackHandlerTest {

    private DispatchBusinessProcessCallbackHandler handler;

    @BeforeEach
    public void setUp() {
        handler = new DispatchBusinessProcessCallbackHandler(new ObjectMapper());
    }

    @Nested
    class AboutToStartCallback {

        @ParameterizedTest
        @EnumSource(value = BusinessProcessStatus.class, mode = EnumSource.Mode.EXCLUDE, names = {"READY"})
        void shouldReturnError_whenBusinessProcessIsStarted(BusinessProcessStatus status) {
            CaseData caseData = CaseDataBuilder.builder()
                .businessProcess(businessProcessWithStatus(status))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsOnly("Business process already started");
        }

        @Test
        void shouldNotReturnError_whenBusinessProcessIsNotReady() {
            CaseData caseData = CaseDataBuilder.builder()
                .businessProcess(businessProcessWithStatus(READY))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldDispatchBusinessProcess_whenStatusIsReady() {
            CaseData caseData = CaseDataBuilder.builder()
                .businessProcess(businessProcessWithStatus(READY))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status", "camundaEvent")
                .containsExactly(DISPATCHED.name(), "testCamundaEvent");
        }

        @ParameterizedTest
        @EnumSource(value = BusinessProcessStatus.class, mode = EnumSource.Mode.EXCLUDE, names = {"READY"})
        void shouldNotDispatchBusinessProcess_whenStatusIsNotReady(BusinessProcessStatus status) {
            CaseData caseData = CaseDataBuilder.builder()
                .businessProcess(businessProcessWithStatus(status))
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("businessProcess").extracting("status").isEqualTo(status.name());
            assertThat(response.getData()).extracting("businessProcess").extracting("camundaEvent").isEqualTo(
                "testCamundaEvent");
            assertThat(response.getData()).extracting("businessProcess").extracting("activityId").isEqualTo(
                "testActivityId");
            assertThat(response.getData()).extracting("businessProcess").extracting("processInstanceId").isEqualTo(
                "testProcessInstanceId");
        }
    }

    private BusinessProcess businessProcessWithStatus(BusinessProcessStatus status) {
        return BusinessProcess.builder()
            .camundaEvent("testCamundaEvent")
            .activityId("testActivityId")
            .processInstanceId("testProcessInstanceId")
            .status(status)
            .build();
    }
}
