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
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.CourtStaffNextSteps;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ObligationData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
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

    private static final String HEADER_CONFIRMATION = "# The order review has been completed";
    private static final String BODY_CONFIRMATION_NO_OBLIGATION = "&nbsp;";
    private static final String BODY_CONFIRMATION_OBLIGATION = "### What happens next \n\n" +
        "A new task will be generated on the review date.";

    private static final String TASKS_LEFT_ERROR_1 = "Order review not completed.";
    private static final String TASKS_LEFT_ERROR_2 = "You must complete the tasks in the order before you can submit your order review.";
    private static final String TASKS_LEFT_ERROR_3 = "Once you have completed the task you can submit your order review by clicking on the link on your task list.";
    private static final String OBLIGATION_DATE_ERROR = "The obligation date must be in the future";

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

            assertThat(response.getData()).extracting("obligationDatePresent").isNull();
            assertThat(response.getData()).extracting("courtStaffNextSteps").isNull();
            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class MidValidateDate {
        private final String eventName = "validate-obligation-date";

        @Test
        void shouldThrowError_ifObligationDateIsNotInTheFuture() {
            CaseData caseData = CaseData.builder().obligationData(
                List.of(Element.<ObligationData>builder().id(UUID.randomUUID()).value(
                ObligationData.builder().obligationDate(LocalDate.now().minusDays(1)).build()).build())).build();

            CallbackParams params = callbackParamsOf(caseData, MID, eventName);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors().get(0))
                .isEqualTo(OBLIGATION_DATE_ERROR);
        }
    }

    @Nested
    class Mid {
        private String eventName = "validate-tasks-left";

        @Test
        void shouldThrowError_ifStillTasksLeft() {
            CaseData caseData = CaseData.builder()
                .obligationDatePresent(YesOrNo.NO)
                .courtStaffNextSteps(CourtStaffNextSteps.STILL_TASKS)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, eventName);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors().get(0))
                .isEqualTo(TASKS_LEFT_ERROR_1);
            assertThat(response.getErrors().get(1))
                .isEqualTo(TASKS_LEFT_ERROR_2);
            assertThat(response.getErrors().get(2))
                .isEqualTo(TASKS_LEFT_ERROR_3);
        }

        @Test
        void shouldThrowNoError_ifNoTasksLeft() {
            CaseData caseData = CaseData.builder()
                .obligationDatePresent(YesOrNo.NO)
                .courtStaffNextSteps(CourtStaffNextSteps.NO_TASKS)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, eventName);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmit {

        @Test
        void shouldConfirmOrderReview_whenInvoked() {

            CaseData caseData = CaseData.builder()
                .obligationDatePresent(YesOrNo.YES)
                .courtStaffNextSteps(CourtStaffNextSteps.NO_TASKS)
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

        @Test
        void shouldSetAllFinalOrdersIssuedState_whenIsFinalOrder() {
            Mockito.when(toggleService.isCaseEventsEnabled()).thenReturn(true);
            CaseData caseData = CaseData.builder()
                .isFinalOrder(YesOrNo.YES)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getState()).isEqualTo(CaseState.All_FINAL_ORDERS_ISSUED.name());
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnConfirmationBodyInResponse_whenInvoked() {
            Mockito.when(toggleService.isCaseEventsEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted()
                .build();
            caseData.builder().obligationDatePresent(YesOrNo.NO).build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualTo(SubmittedCallbackResponse
                                               .builder()
                                               .confirmationHeader(HEADER_CONFIRMATION)
                                               .confirmationBody(BODY_CONFIRMATION_NO_OBLIGATION)
                                               .build());
        }

        @Test
        void shouldReturnConfirmationBodyWithTextInResponse_whenInvoked() {
            Mockito.when(toggleService.isCaseEventsEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted()
                .build();
            caseData = caseData.builder().obligationDatePresent(YesOrNo.YES).build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualTo(SubmittedCallbackResponse
                                               .builder()
                                               .confirmationHeader(HEADER_CONFIRMATION)
                                               .confirmationBody(BODY_CONFIRMATION_OBLIGATION)
                                               .build());
        }

        @Test
        void shouldReturnEmptyResponse_whenInvoked() {
            Mockito.when(toggleService.isCaseEventsEnabled()).thenReturn(false);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted()
                .build();

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualTo(SubmittedCallbackResponse.builder().build());
        }

    }
}
