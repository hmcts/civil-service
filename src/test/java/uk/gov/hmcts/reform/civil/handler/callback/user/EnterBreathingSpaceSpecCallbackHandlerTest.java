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

import java.time.LocalDate;

public class EnterBreathingSpaceSpecCallbackHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EnterBreathingSpaceSpecCallbackHandler callbackHandler
        = new EnterBreathingSpaceSpecCallbackHandler(objectMapper);

    @Test
    public void canEnterOnce() {
        CaseData caseData = CaseData.builder().build();

        CallbackParams params = CallbackParams.builder()
            .caseData(caseData)
            .type(CallbackType.ABOUT_TO_START)
            .build();
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
        Assertions.assertFalse(response.getErrors() != null && !response.getErrors().isEmpty());
    }

    @Test
    public void cantEnterTwice() {
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
        Assertions.assertFalse(response.getErrors().isEmpty());
    }

    @Test
    public void whenStartDateIsNotPast_thenReturnError() {
        CaseData caseData = CaseData.builder()
            .breathing(BreathingSpaceInfo.builder()
                           .enter(BreathingSpaceEnterInfo.builder()
                                      .start(LocalDate.now().plusDays(1))
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
    public void whenStartDateIsPast_thenReturnNoError() {
        CaseData caseData = CaseData.builder()
            .breathing(BreathingSpaceInfo.builder()
                           .enter(BreathingSpaceEnterInfo.builder()
                                      .start(LocalDate.now())
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
    public void whenEndDateIsNotFuture_thenReturnError() {
        CaseData caseData = CaseData.builder()
            .breathing(BreathingSpaceInfo.builder()
                           .enter(BreathingSpaceEnterInfo.builder()
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
        Assertions.assertFalse(response.getErrors().isEmpty());
    }

    @Test
    public void whenEndDateIsFuture_thenReturnNoError() {
        CaseData caseData = CaseData.builder()
            .breathing(BreathingSpaceInfo.builder()
                           .enter(BreathingSpaceEnterInfo.builder()
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
        Assertions.assertTrue(response.getErrors().isEmpty());
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
