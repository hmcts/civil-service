package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CONFIRM_ORDER_REVIEW;

@SpringBootTest(classes = {
    ConfirmOrderReviewCallbackHandler.class,
    JacksonAutoConfiguration.class,
})
class ConfirmOrderReviewCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private ConfirmOrderReviewCallbackHandler handler;

    @MockBean
    private FeatureToggleService toggleService;

    @BeforeEach
    void caseEventsEnabled() {
        Mockito.when(toggleService.isCaseEventsEnabled()).thenReturn(true);
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class AboutToSubmit {

        @Test
        void shouldConfirmOrderReview_whenInvoked() {

            CaseData caseData = CaseData.builder()
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(CONFIRM_ORDER_REVIEW.name(), "READY");
        }

        @Test
        void shouldReturnEmptyResponse_whenInvoked() {
            Mockito.when(toggleService.isCaseEventsEnabled()).thenReturn(false);
            CaseData caseData = CaseData.builder()
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isEqualTo(AboutToStartOrSubmitCallbackResponse.builder().build());
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnEmptyResponse_whenInvoked() {
            Mockito.when(toggleService.isCaseEventsEnabled()).thenReturn(false);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted()
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualTo(SubmittedCallbackResponse.builder().build());
        }

    }
}
