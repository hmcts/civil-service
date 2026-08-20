package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceEnterInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.LIFT_BREATHING_SPACE_SPEC;

public class LiftBreathingSpaceSpecCallbackHandlerTest extends BaseCallbackHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);

    private final LiftBreathingSpaceSpecCallbackHandler callbackHandler =
        new LiftBreathingSpaceSpecCallbackHandler(objectMapper, caseDetailsConverter);

    @Nested
    class AboutToStartCallback {

        @Test
        public void cantEnterIfNoBreathingSpaceYet() {
            CaseData caseData = CaseDataBuilder.builder().build();

            CallbackParams params = new CallbackParams()
                .caseData(caseData)
                .type(CallbackType.ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
            Assertions.assertFalse(response.getErrors().isEmpty());
        }

        @Test
        public void canEnterIfWithinBreathingSpace() {
            CaseData caseData = CaseDataBuilder.builder().build();
            BreathingSpaceInfo breathingSpaceInfo = new BreathingSpaceInfo();
            breathingSpaceInfo.setEnter(new BreathingSpaceEnterInfo());
            caseData.setBreathing(breathingSpaceInfo);

            CallbackParams params = new CallbackParams()
                .caseData(caseData)
                .type(CallbackType.ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
            Assertions.assertTrue(response.getErrors() == null || response.getErrors().isEmpty());
        }

        @Test
        public void cantEnterIfAlreadyLifted() {
            CaseData caseData = CaseDataBuilder.builder().build();
            BreathingSpaceInfo breathingSpaceInfo = new BreathingSpaceInfo();
            breathingSpaceInfo.setEnter(new BreathingSpaceEnterInfo());
            breathingSpaceInfo.setLift(new BreathingSpaceLiftInfo());
            caseData.setBreathing(breathingSpaceInfo);

            CallbackParams params = new CallbackParams()
                .caseData(caseData)
                .type(CallbackType.ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
            Assertions.assertFalse(response.getErrors().isEmpty());
        }

        @Test
        public void prePopulateEndDateToToday_WhenMentalHealthBSType() {
            BreathingSpaceEnterInfo breathingSpaceEnterInfo = new BreathingSpaceEnterInfo();
            breathingSpaceEnterInfo.setStart(LocalDate.now());
            breathingSpaceEnterInfo.setType(BreathingSpaceType.MENTAL_HEALTH);

            BreathingSpaceInfo breathingSpaceInfo = new BreathingSpaceInfo();
            breathingSpaceInfo.setEnter(breathingSpaceEnterInfo);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setBreathing(breathingSpaceInfo);

            CallbackParams params = new CallbackParams()
                .caseData(caseData)
                .type(CallbackType.ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
            LocalDate expectedEndDate = LocalDate.now();
            assertEquals(caseData.getBreathing().getLift().getExpectedEnd(), expectedEndDate);
        }

        @Test
        public void prePopulateEndDateToStartDatePlus60_WhenStandardBSType() {
            BreathingSpaceEnterInfo breathingSpaceEnterInfo = new BreathingSpaceEnterInfo();
            breathingSpaceEnterInfo.setStart(LocalDate.now());
            breathingSpaceEnterInfo.setType(BreathingSpaceType.STANDARD);

            BreathingSpaceInfo breathingSpaceInfo = new BreathingSpaceInfo();
            breathingSpaceInfo.setEnter(breathingSpaceEnterInfo);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setBreathing(breathingSpaceInfo);

            CallbackParams params = new CallbackParams()
                .caseData(caseData)
                .type(CallbackType.ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
            LocalDate expectedEndDate = LocalDate.now().plusDays(60);
            assertEquals(caseData.getBreathing().getLift().getExpectedEnd(), expectedEndDate);
        }
    }

    @Nested
    class MidCallback {

        @Test
        public void whenEndDateExceeds60DaysForStandardBS_thenReturnError() {
            BreathingSpaceInfo breathingSpaceInfo = new BreathingSpaceInfo();
            BreathingSpaceEnterInfo breathingSpaceEnterInfo = new BreathingSpaceEnterInfo();
            breathingSpaceEnterInfo.setStart(LocalDate.now());
            breathingSpaceEnterInfo.setType(BreathingSpaceType.STANDARD);
            breathingSpaceInfo.setEnter(breathingSpaceEnterInfo);
            BreathingSpaceLiftInfo breathingSpaceLiftInfo = new BreathingSpaceLiftInfo();
            breathingSpaceLiftInfo.setExpectedEnd(LocalDate.now().plusDays(61));
            breathingSpaceInfo.setLift(breathingSpaceLiftInfo);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setBreathing(breathingSpaceInfo);

            CallbackParams params = new CallbackParams()
                .caseData(caseData)
                .type(CallbackType.MID)
                .pageId("enter-info");
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
            assertThat(response.getErrors().getFirst()).isEqualTo("Standard breathing space cannot last for longer than 60 days");
        }

        @Test
        public void whenEndDateExceeds60DaysForMentalHealthBS_thenReturnNoError() {
            BreathingSpaceInfo breathingSpaceInfo = new BreathingSpaceInfo();
            BreathingSpaceEnterInfo breathingSpaceEnterInfo = new BreathingSpaceEnterInfo();
            breathingSpaceEnterInfo.setStart(LocalDate.now());
            breathingSpaceEnterInfo.setType(BreathingSpaceType.MENTAL_HEALTH);
            breathingSpaceInfo.setEnter(breathingSpaceEnterInfo);
            BreathingSpaceLiftInfo breathingSpaceLiftInfo = new BreathingSpaceLiftInfo();
            breathingSpaceLiftInfo.setExpectedEnd(LocalDate.now().plusDays(61));
            breathingSpaceInfo.setLift(breathingSpaceLiftInfo);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setBreathing(breathingSpaceInfo);

            CallbackParams params = new CallbackParams()
                .caseData(caseData)
                .type(CallbackType.MID)
                .pageId("enter-info");
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
            Assertions.assertTrue(response.getErrors().isEmpty());
        }

        @Test
        public void whenEndDateIsNotAfterStartDate_thenReturnError() {
            BreathingSpaceInfo breathingSpaceInfo = new BreathingSpaceInfo();
            BreathingSpaceEnterInfo breathingSpaceEnterInfo = new BreathingSpaceEnterInfo();
            breathingSpaceEnterInfo.setStart(LocalDate.now().minusDays(30));
            breathingSpaceInfo.setEnter(breathingSpaceEnterInfo);
            BreathingSpaceLiftInfo breathingSpaceLiftInfo = new BreathingSpaceLiftInfo();
            breathingSpaceLiftInfo.setExpectedEnd(LocalDate.now().minusDays(31));
            breathingSpaceInfo.setLift(breathingSpaceLiftInfo);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setBreathing(breathingSpaceInfo);

            CallbackParams params = new CallbackParams()
                .caseData(caseData)
                .type(CallbackType.MID)
                .pageId("enter-info");
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
            Assertions.assertFalse(response.getErrors().isEmpty());
        }

        @Test
        public void whenEndDateIsSameDateAsStartDateForStandardBS_thenReturnError() {
            BreathingSpaceInfo breathingSpaceInfo = new BreathingSpaceInfo();
            BreathingSpaceEnterInfo breathingSpaceEnterInfo = new BreathingSpaceEnterInfo();
            breathingSpaceEnterInfo.setStart(LocalDate.now());
            breathingSpaceEnterInfo.setType(BreathingSpaceType.STANDARD);
            breathingSpaceInfo.setEnter(breathingSpaceEnterInfo);
            BreathingSpaceLiftInfo breathingSpaceLiftInfo = new BreathingSpaceLiftInfo();
            breathingSpaceLiftInfo.setExpectedEnd(LocalDate.now());
            breathingSpaceInfo.setLift(breathingSpaceLiftInfo);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setBreathing(breathingSpaceInfo);

            CallbackParams params = new CallbackParams()
                .caseData(caseData)
                .type(CallbackType.MID)
                .pageId("enter-info");
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
            Assertions.assertFalse(response.getErrors().isEmpty());
        }

        @Test
        public void whenEndDateIsSameDateAsStartDateForMentalHealthBS_thenReturnNoError() {
            BreathingSpaceInfo breathingSpaceInfo = new BreathingSpaceInfo();
            BreathingSpaceEnterInfo breathingSpaceEnterInfo = new BreathingSpaceEnterInfo();
            breathingSpaceEnterInfo.setStart(LocalDate.now());
            breathingSpaceEnterInfo.setType(BreathingSpaceType.MENTAL_HEALTH);
            breathingSpaceInfo.setEnter(breathingSpaceEnterInfo);
            BreathingSpaceLiftInfo breathingSpaceLiftInfo = new BreathingSpaceLiftInfo();
            breathingSpaceLiftInfo.setExpectedEnd(LocalDate.now());
            breathingSpaceInfo.setLift(breathingSpaceLiftInfo);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setBreathing(breathingSpaceInfo);

            CallbackParams params = new CallbackParams()
                .caseData(caseData)
                .type(CallbackType.MID)
                .pageId("enter-info");
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
            Assertions.assertTrue(response.getErrors().isEmpty());
        }

        @Test
        public void whenEndDateIsAfterStartDate_thenReturnNoError() {
            BreathingSpaceInfo breathingSpaceInfo = new BreathingSpaceInfo();
            BreathingSpaceEnterInfo breathingSpaceEnterInfo = new BreathingSpaceEnterInfo();
            breathingSpaceEnterInfo.setStart(LocalDate.now());
            breathingSpaceInfo.setEnter(breathingSpaceEnterInfo);
            BreathingSpaceLiftInfo breathingSpaceLiftInfo = new BreathingSpaceLiftInfo();
            breathingSpaceLiftInfo.setExpectedEnd(LocalDate.now().plusDays(1));
            breathingSpaceInfo.setLift(breathingSpaceLiftInfo);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setBreathing(breathingSpaceInfo);

            CallbackParams params = new CallbackParams()
                .caseData(caseData)
                .type(CallbackType.MID)
                .pageId("enter-info");
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
            Assertions.assertTrue(response.getErrors().isEmpty());
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        public void shouldUpdateBusinessProcess_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(LIFT_BREATHING_SPACE_SPEC.name(), "READY");

            assertThat(response.getData())
                .extracting("breathingSpaceLifted")
                .isEqualTo("Yes");
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        public void whenSubmitted_thenIncludeHeader() {
            String claimNumber = "claim number";
            CaseData caseData = CaseDataBuilder.builder()
                .legacyCaseReference(claimNumber)
                .build();

            CallbackParams params = new CallbackParams()
                .caseData(caseData)
                .type(CallbackType.SUBMITTED);
            SubmittedCallbackResponse response =
                (SubmittedCallbackResponse) callbackHandler.handle(params);
            Assertions.assertTrue(response.getConfirmationHeader().contains(claimNumber));
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(callbackHandler.handledEvents()).contains(LIFT_BREATHING_SPACE_SPEC);
    }
}
