package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceEnterInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ENTER_BREATHING_SPACE_SPEC;

public class EnterBreathingSpaceSpecCallbackHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EnterBreathingSpaceSpecCallbackHandler callbackHandler
        = new EnterBreathingSpaceSpecCallbackHandler(objectMapper);

    @Test
    public void canEnterOnce() {
        CaseData caseData = CaseData.builder().build();

        CallbackParams params = new CallbackParams()
            .caseData(caseData)
            .type(CallbackType.ABOUT_TO_START);
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
        Assertions.assertFalse(response.getErrors() != null && !response.getErrors().isEmpty());
    }

    @Test
    public void cantEnterTwice() {
        BreathingSpaceEnterInfo enterInfo = new BreathingSpaceEnterInfo();
        BreathingSpaceInfo breathingInfo = new BreathingSpaceInfo();
        breathingInfo.setEnter(enterInfo);
        CaseData caseData = CaseData.builder().build();
        caseData.setBreathing(breathingInfo);

        CallbackParams params = new CallbackParams()
            .caseData(caseData)
            .type(CallbackType.ABOUT_TO_START);
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
        Assertions.assertFalse(response.getErrors().isEmpty());
    }

    @Test
    public void whenStartDateIsNotPast_thenReturnError() {
        BreathingSpaceEnterInfo enterInfo = new BreathingSpaceEnterInfo();
        enterInfo.setStart(LocalDate.now().plusDays(1));
        BreathingSpaceInfo breathingInfo = new BreathingSpaceInfo();
        breathingInfo.setEnter(enterInfo);
        CaseData caseData = CaseData.builder().build();
        caseData.setBreathing(breathingInfo);

        CallbackParams params = new CallbackParams()
            .caseData(caseData)
            .type(CallbackType.MID)
            .pageId("enter-info");
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
        Assertions.assertFalse(response.getErrors().isEmpty());
    }

    @Test
    public void whenStartDateIsPast_thenReturnNoError() {
        BreathingSpaceEnterInfo enterInfo = new BreathingSpaceEnterInfo();
        enterInfo.setStart(LocalDate.now());
        BreathingSpaceInfo breathingInfo = new BreathingSpaceInfo();
        breathingInfo.setEnter(enterInfo);
        CaseData caseData = CaseData.builder().build();
        caseData.setBreathing(breathingInfo);

        CallbackParams params = new CallbackParams()
            .caseData(caseData)
            .type(CallbackType.MID)
            .pageId("enter-info");
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
        Assertions.assertTrue(response.getErrors().isEmpty());
    }

    @Test
    public void whenEndDateIsNotFuture_thenReturnError() {
        BreathingSpaceEnterInfo enterInfo = new BreathingSpaceEnterInfo();
        enterInfo.setExpectedEnd(LocalDate.now());
        BreathingSpaceInfo breathingInfo = new BreathingSpaceInfo();
        breathingInfo.setEnter(enterInfo);
        CaseData caseData = CaseData.builder().build();
        caseData.setBreathing(breathingInfo);

        CallbackParams params = new CallbackParams()
            .caseData(caseData)
            .type(CallbackType.MID)
            .pageId("enter-info");
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
        Assertions.assertFalse(response.getErrors().isEmpty());
    }

    @Test
    public void whenEndDateIsFuture_thenReturnNoError() {
        BreathingSpaceEnterInfo enterInfo = new BreathingSpaceEnterInfo();
        enterInfo.setExpectedEnd(LocalDate.now().plusDays(1));
        BreathingSpaceInfo breathingInfo = new BreathingSpaceInfo();
        breathingInfo.setEnter(enterInfo);
        CaseData caseData = CaseData.builder().build();
        caseData.setBreathing(breathingInfo);

        CallbackParams params = new CallbackParams()
            .caseData(caseData)
            .type(CallbackType.MID)
            .pageId("enter-info");
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
        Assertions.assertTrue(response.getErrors().isEmpty());
    }

    @Test
    public void whenSubmitted_thenIncludeHeader() {
        String claimNumber = "claim number";
        CaseData caseData = CaseData.builder().build();
        caseData.setLegacyCaseReference(claimNumber);

        CallbackParams params = new CallbackParams()
            .caseData(caseData)
            .type(CallbackType.SUBMITTED);
        SubmittedCallbackResponse response =
            (SubmittedCallbackResponse) callbackHandler.handle(params);
        Assertions.assertTrue(response.getConfirmationHeader().contains(claimNumber));
    }

    @Test
    void testAboutToSubmitCallback() {
        CaseData caseData = CaseData.builder().build();

        CallbackParams params = new CallbackParams()
            .caseData(caseData)
            .type(CallbackType.ABOUT_TO_SUBMIT);
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
        Assertions.assertTrue(response.getData().containsKey("businessProcess"));
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(callbackHandler.handledEvents()).containsOnly(ENTER_BREATHING_SPACE_SPEC);
    }
}
