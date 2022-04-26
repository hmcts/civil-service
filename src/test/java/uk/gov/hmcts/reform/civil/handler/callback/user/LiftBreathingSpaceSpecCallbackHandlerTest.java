package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceEnterInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;

import java.time.LocalDate;

public class LiftBreathingSpaceSpecCallbackHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LiftBreathingSpaceSpecCallbackHandler callbackHandler =
        new LiftBreathingSpaceSpecCallbackHandler(objectMapper);

    @Test
    public void cantEnterIfNoBreathingSpaceYet() {
        CaseData caseData = CaseData.builder().build();

        CallbackParams params = CallbackParams.builder()
            .caseData(caseData)
            .type(CallbackType.ABOUT_TO_START)
            .build();
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
        Assertions.assertFalse(response.getErrors().isEmpty());
    }

    @Test
    public void canEnterIfWithinBreathingSpace() {
        CaseData caseData = CaseData.builder()
            .breathing(BreathingSpaceInfo.builder()
                           .enter(BreathingSpaceEnterInfo.builder().build())
                           .build())
            .build();

        CallbackParams params = CallbackParams.builder()
            .caseData(caseData)
            .type(CallbackType.ABOUT_TO_START)
            .build();
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
        Assertions.assertTrue(response.getErrors() == null || response.getErrors().isEmpty());
    }

    @Test
    public void cantEnterIfAlreadyLifted() {
        CaseData caseData = CaseData.builder()
            .breathing(BreathingSpaceInfo.builder()
                           .enter(BreathingSpaceEnterInfo.builder().build())
                           .lift(BreathingSpaceLiftInfo.builder().build())
                           .build())
            .build();

        CallbackParams params = CallbackParams.builder()
            .caseData(caseData)
            .type(CallbackType.ABOUT_TO_START)
            .build();
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
        Assertions.assertFalse(response.getErrors().isEmpty());
    }

    @Test
    public void whenEndDateIsFuture_thenReturnError() {
        CaseData caseData = CaseData.builder()
            .breathing(BreathingSpaceInfo.builder()
                           .enter(BreathingSpaceEnterInfo.builder().build())
                           .lift(BreathingSpaceLiftInfo.builder()
                                     .expectedEnd(LocalDate.now().plusDays(1))
                                     .build())
                           .build())
            .build();

        CallbackParams params = CallbackParams.builder()
            .caseData(caseData)
            .type(CallbackType.MID)
            .pageId("enter-info")
            .build();
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
        Assertions.assertFalse(response.getErrors().isEmpty());
    }

    @Test
    public void whenEndDateIsNotFuture_thenReturnNoError() {
        CaseData caseData = CaseData.builder()
            .breathing(BreathingSpaceInfo.builder()
                           .enter(BreathingSpaceEnterInfo.builder().build())
                           .lift(BreathingSpaceLiftInfo.builder()
                                     .expectedEnd(LocalDate.now())
                                     .build())
                           .build())
            .build();

        CallbackParams params = CallbackParams.builder()
            .caseData(caseData)
            .type(CallbackType.MID)
            .pageId("enter-info")
            .build();
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
        Assertions.assertTrue(response.getErrors().isEmpty());
    }

    @Test
    public void whenDatesDoNotMatch_thenReturnError() {
        CaseData caseData = CaseData.builder()
            .breathing(BreathingSpaceInfo.builder()
                           .enter(BreathingSpaceEnterInfo.builder()
                                      .start(LocalDate.now().minusDays(30))
                                      .build())
                           .lift(BreathingSpaceLiftInfo.builder()
                                     .expectedEnd(LocalDate.now().minusDays(31))
                                     .build())
                           .build())
            .build();

        CallbackParams params = CallbackParams.builder()
            .caseData(caseData)
            .type(CallbackType.MID)
            .pageId("enter-info")
            .build();
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
        Assertions.assertFalse(response.getErrors().isEmpty());
    }

    @Test
    public void whenSubmitted_thenIncludeHeader() {
        String claimNumber = "claim number";
        CaseData caseData = CaseData.builder()
            .legacyCaseReference(claimNumber)
            .build();

        CallbackParams params = CallbackParams.builder()
            .caseData(caseData)
            .type(CallbackType.SUBMITTED)
            .build();
        SubmittedCallbackResponse response =
            (SubmittedCallbackResponse) callbackHandler.handle(params);
        Assertions.assertTrue(response.getConfirmationHeader().contains(claimNumber));
    }
}
