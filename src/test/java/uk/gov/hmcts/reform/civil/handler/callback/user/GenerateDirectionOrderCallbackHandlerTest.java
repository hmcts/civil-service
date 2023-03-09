package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DIRECTIONS_ORDER;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateDirectionOrderCallbackHandler.class,
    JacksonAutoConfiguration.class
})
public class GenerateDirectionOrderCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private GenerateDirectionOrderCallbackHandler handler;

    private static final String onInitiativeSelectionText = "As this order was made on the court's own initiative "
        + "any party affected by the order may apply to set aside, vary or stay the order. Any such application must "
        + "be made by 4pm on";
    private static final String withoutNoticeSelectionText = "If you were not notified of the application before "
        + "this order was made, you may apply to set aside, vary or stay the order. Any such application must be made "
        + "by 4pm on";


    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
        }
    }

    @Test
    void shouldPopulateFreeFormOrderValues_onMidEventCallback() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, "populate-freeForm-values");
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getData()).extracting("orderOnCourtInitiative").extracting("onInitiativeSelectionTextArea")
            .isEqualTo(onInitiativeSelectionText);
        assertThat(response.getData()).extracting("orderOnCourtInitiative").extracting("onInitiativeSelectionDate")
            .isEqualTo(LocalDate.now().toString());
        assertThat(response.getData()).extracting("orderWithoutNotice").extracting("withoutNoticeSelectionTextArea")
            .isEqualTo(withoutNoticeSelectionText);
        assertThat(response.getData()).extracting("orderWithoutNotice").extracting("withoutNoticeSelectionDate")
            .isEqualTo(LocalDate.now().toString());

    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldReturnNoError_WhenAboutToSubmitIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnNoError_WhenSubmittedIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            var response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response.getConfirmationBody()).isNull();
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(GENERATE_DIRECTIONS_ORDER);
    }

}
