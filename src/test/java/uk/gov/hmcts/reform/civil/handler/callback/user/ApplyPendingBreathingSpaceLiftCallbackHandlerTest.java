package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.LIFT_BREATHING_SPACE_SPEC;

class ApplyPendingBreathingSpaceLiftCallbackHandlerTest extends BaseCallbackHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final ApplyPendingBreathingSpaceLiftCallbackHandler callbackHandler =
        new ApplyPendingBreathingSpaceLiftCallbackHandler(objectMapper);

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldLiftBreathingSpace_whenExpectedEndIsToday() {
            CaseData caseData = pendingLift(LocalDate.now());

            var response = (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(
                callbackParamsOf(caseData, ABOUT_TO_SUBMIT)
            );

            assertThat(response.getData())
                .extracting("breathingSpaceActive")
                .isEqualTo("No");
            assertThat(response.getData())
                .extracting("breathingSpaceLiftPending")
                .isEqualTo("No");
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(LIFT_BREATHING_SPACE_SPEC.name(), "READY");
        }

        @Test
        void shouldLiftBreathingSpace_whenExpectedEndIsBeforeToday() {
            CaseData caseData = pendingLift(LocalDate.now().minusDays(1));

            var response = (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(
                callbackParamsOf(caseData, ABOUT_TO_SUBMIT)
            );

            assertThat(response.getData())
                .extracting("breathingSpaceActive")
                .isEqualTo("No");
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(LIFT_BREATHING_SPACE_SPEC.name(), "READY");
        }

        @Test
        void shouldLeaveBreathingSpaceActive_whenExpectedEndIsAfterToday() {
            CaseData caseData = pendingLift(LocalDate.now().plusDays(1));

            var response = (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(
                callbackParamsOf(caseData, ABOUT_TO_SUBMIT)
            );

            assertThat(response.getData())
                .extracting("breathingSpaceActive")
                .isEqualTo("Yes");
            assertThat(response.getData())
                .extracting("breathingSpaceLiftPending")
                .isEqualTo("Yes");
            assertThat(response.getData().get("businessProcess")).isNull();
        }
    }

    private CaseData pendingLift(LocalDate expectedEnd) {
        BreathingSpaceLiftInfo liftInfo = new BreathingSpaceLiftInfo();
        liftInfo.setExpectedEnd(expectedEnd);
        BreathingSpaceInfo breathingSpaceInfo = new BreathingSpaceInfo();
        breathingSpaceInfo.setActive(YesOrNo.YES);
        breathingSpaceInfo.setLiftPending(YesOrNo.YES);
        breathingSpaceInfo.setLift(liftInfo);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        caseData.setBreathing(breathingSpaceInfo);
        return caseData;
    }
}
