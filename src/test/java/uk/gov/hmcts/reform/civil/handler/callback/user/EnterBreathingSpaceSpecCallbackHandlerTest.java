package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceEnterInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.breathing.StoredBreathingSpace;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.utils.BreathingSpaceUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ENTER_BREATHING_SPACE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.BreathingSpaceUtils.ALL_DEFENDANTS_LEFT;
import static uk.gov.hmcts.reform.civil.utils.BreathingSpaceUtils.ALREADY_IN_BREATHING_SPACE;

public class EnterBreathingSpaceSpecCallbackHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
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
        breathingInfo.setActive(YES);
        CaseData caseData = CaseData.builder().build();
        caseData.setBreathing(breathingInfo);

        CallbackParams params = new CallbackParams()
            .caseData(caseData)
            .type(CallbackType.ABOUT_TO_START);
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
        Assertions.assertFalse(response.getErrors().isEmpty());
        Assertions.assertEquals(ALREADY_IN_BREATHING_SPACE, response.getErrors().getFirst());
    }

    @Test
    public void cantEnterAgainWhenSingleDefendantHasLeft() {
        BreathingSpaceInfo breathingInfo = new BreathingSpaceInfo();
        breathingInfo.setEnter(new BreathingSpaceEnterInfo());
        CaseData caseData = CaseData.builder().build();
        caseData.setBreathing(breathingInfo);
        BreathingSpaceUtils.addEnteredBreathingSpaceToHistory(caseData);
        breathingInfo.setLift(new BreathingSpaceLiftInfo());
        BreathingSpaceUtils.addLiftDetailsToCurrentBreathingSpace(caseData);

        CallbackParams params = new CallbackParams()
            .caseData(caseData)
            .type(CallbackType.ABOUT_TO_START);
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
        Assertions.assertEquals(ALL_DEFENDANTS_LEFT, response.getErrors().getFirst());
    }

    @Test
    public void canEnterAgainOnOneVTwoAfterFirstLift() {
        BreathingSpaceInfo breathingInfo = new BreathingSpaceInfo();
        breathingInfo.setEnter(new BreathingSpaceEnterInfo());
        CaseData caseData = CaseData.builder().addRespondent2(YES).build();
        caseData.setBreathing(breathingInfo);
        BreathingSpaceUtils.addEnteredBreathingSpaceToHistory(caseData);
        breathingInfo.setLift(new BreathingSpaceLiftInfo());
        BreathingSpaceUtils.addLiftDetailsToCurrentBreathingSpace(caseData);

        CallbackParams params = new CallbackParams()
            .caseData(caseData)
            .type(CallbackType.ABOUT_TO_START);
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
        Assertions.assertTrue(response.getErrors() == null || response.getErrors().isEmpty());
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
    public void whenStartDateFieldIsNotPresent_thenDefaultToToday() {
        BreathingSpaceEnterInfo enterInfo = new BreathingSpaceEnterInfo();
        BreathingSpaceInfo breathingInfo = new BreathingSpaceInfo();
        breathingInfo.setEnter(enterInfo);
        CaseData caseData = CaseData.builder().build();
        caseData.setBreathing(breathingInfo);

        CallbackParams params = new CallbackParams()
            .caseData(caseData)
            .type(CallbackType.ABOUT_TO_SUBMIT);
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
        assertThat(response.getData())
            .extracting("enterBreathing")
            .extracting("start")
            .isEqualTo(LocalDate.now().toString());
    }

    @Test
    public void whenStartDateFieldIsPresent_thenDontDefaultToToday() {
        BreathingSpaceEnterInfo enterInfo = new BreathingSpaceEnterInfo();
        enterInfo.setStart(LocalDate.now().minusDays(1));
        BreathingSpaceInfo breathingInfo = new BreathingSpaceInfo();
        breathingInfo.setEnter(enterInfo);
        CaseData caseData = CaseData.builder().build();
        caseData.setBreathing(breathingInfo);

        CallbackParams params = new CallbackParams()
            .caseData(caseData)
            .type(CallbackType.ABOUT_TO_SUBMIT);
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
        assertThat(response.getData())
            .extracting("enterBreathing")
            .extracting("start")
            .isEqualTo(LocalDate.now().minusDays(1).toString());
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
        caseData.setBreathing(new BreathingSpaceInfo());

        CallbackParams params = new CallbackParams()
            .caseData(caseData)
            .type(CallbackType.ABOUT_TO_SUBMIT);
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);
        Assertions.assertTrue(response.getData().containsKey("businessProcess"));
        Assertions.assertEquals("Yes", response.getData().get("breathingSpaceActive"));
    }

    @Test
    void aboutToSubmitStoresEnterDetailsAndClearsLift() {
        BreathingSpaceEnterInfo enterInfo = new BreathingSpaceEnterInfo();
        enterInfo.setStart(LocalDate.now().minusDays(1));
        BreathingSpaceInfo breathingInfo = new BreathingSpaceInfo();
        breathingInfo.setEnter(enterInfo);
        breathingInfo.setLift(new BreathingSpaceLiftInfo());
        CaseData caseData = CaseData.builder().build();
        caseData.setBreathing(breathingInfo);

        CallbackParams params = new CallbackParams()
            .caseData(caseData)
            .type(CallbackType.ABOUT_TO_SUBMIT);
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);

        List<Element<StoredBreathingSpace>> stored = objectMapper.convertValue(
            response.getData().get("storedBreathingSpace"),
            new TypeReference<>() {}
        );
        assertThat(stored).hasSize(1);
        assertThat(stored.get(0).getValue().getDefendantLabel())
            .isEqualTo("Breathing space for Defendant 1 details");
        assertThat(stored.get(0).getValue().getEnter().getStart())
            .isEqualTo(LocalDate.now().minusDays(1));
        assertThat(stored.get(0).getValue().getLift()).isNull();
        assertThat(response.getData().get("liftBreathing")).isNull();
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(callbackHandler.handledEvents()).containsOnly(ENTER_BREATHING_SPACE_SPEC);
    }
}
