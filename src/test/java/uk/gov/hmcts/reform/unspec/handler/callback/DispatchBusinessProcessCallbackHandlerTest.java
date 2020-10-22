package uk.gov.hmcts.reform.unspec.handler.callback;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus.DISPATCHED;
import static uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus.READY;

@SpringBootTest(classes = {
    DispatchBusinessProcessCallbackHandler.class
})
class DispatchBusinessProcessCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private DispatchBusinessProcessCallbackHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldDispatchBusinessProcess_whenStatusIsReady() {
            CaseData caseData = CaseDataBuilder.builder()
                .businessProcess(BusinessProcess.builder()
                                     .camundaEvent("testCamundaEvent")
                                     .activityId("testActivityId")
                                     .processInstanceId("testProcessInstanceId")
                                     .status(READY)
                                     .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("businessProcess").extracting("status").isEqualTo(DISPATCHED);
            assertThat(response.getData()).extracting("businessProcess").extracting("camundaEvent").isEqualTo(
                "testCamundaEvent");
            assertThat(response.getData()).extracting("businessProcess").extracting("activityId").isNull();
            assertThat(response.getData()).extracting("businessProcess").extracting("processInstanceId").isNull();
        }

        @ParameterizedTest
        @EnumSource(value = BusinessProcessStatus.class, mode = EnumSource.Mode.EXCLUDE, names = {"READY"})
        void shouldNotDispatchBusinessProcess_whenStatusIsNotReady(BusinessProcessStatus status) {
            CaseData caseData = CaseDataBuilder.builder()
                .businessProcess(BusinessProcess.builder()
                                     .camundaEvent("testCamundaEvent")
                                     .activityId("testActivityId")
                                     .processInstanceId("testProcessInstanceId")
                                     .status(status)
                                     .build())
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
}
