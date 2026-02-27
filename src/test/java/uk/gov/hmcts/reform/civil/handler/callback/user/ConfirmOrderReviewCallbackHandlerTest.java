package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.CourtStaffNextSteps;
import uk.gov.hmcts.reform.civil.enums.ObligationReason;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ObligationData;
import uk.gov.hmcts.reform.civil.model.StoredObligationData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CONFIRM_ORDER_REVIEW;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@ExtendWith(MockitoExtension.class)
class ConfirmOrderReviewCallbackHandlerTest extends BaseCallbackHandlerTest {

    private ConfirmOrderReviewCallbackHandler handler;

    @Mock
    private FeatureToggleService toggleService;
    @Mock
    private UserService userService;

    private ObjectMapper objectMapper;

    private static final String HEADER_CONFIRMATION = "# The order review has been completed";
    private static final String BODY_CONFIRMATION_NO_OBLIGATION = "&nbsp;";
    private static final String BODY_CONFIRMATION_OBLIGATION = "### What happens next \n\n" +
        "A new task will be generated on the review date.";

    private static final String TASKS_LEFT_ERROR_1 = "Order review not completed.";
    private static final String TASKS_LEFT_ERROR_2 = "You must complete the tasks in the order before you can submit your order review.";
    private static final String TASKS_LEFT_ERROR_3 = "Once you have completed the task you can submit your order review by clicking on the link on your task list.";
    private static final String OBLIGATION_DATE_ERROR = "The obligation date must be in the future";

    @Mock
    private Time time;

    @BeforeEach
    void caseEventsEnabled() {

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new ConfirmOrderReviewCallbackHandler(toggleService, objectMapper, userService, time);
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
            ObligationData obligationData = new ObligationData();
            obligationData.setObligationDate(LocalDate.now().minusDays(1));
            Element<ObligationData> element = new Element<>();
            element.setId(UUID.randomUUID());
            element.setValue(obligationData);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setObligationData(List.of(element));

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
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setObligationDatePresent(NO);
            caseData.setCourtStaffNextSteps(CourtStaffNextSteps.STILL_TASKS);

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
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setObligationDatePresent(NO);
            caseData.setCourtStaffNextSteps(CourtStaffNextSteps.NO_TASKS);

            CallbackParams params = callbackParamsOf(caseData, MID, eventName);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmit {

        @Test
        void shouldConfirmOrderReview_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setObligationDatePresent(YesOrNo.YES);
            caseData.setCourtStaffNextSteps(CourtStaffNextSteps.NO_TASKS);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(CONFIRM_ORDER_REVIEW.name(), "READY");
        }

        @Test
        void shouldSetAllFinalOrdersIssuedState_whenIsFinalOrder() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setIsFinalOrder(YesOrNo.YES);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getState()).isEqualTo(CaseState.All_FINAL_ORDERS_ISSUED.name());
        }

        @Test
        void shouldSetToDecisionOutcomeState_whenOrderIsNotFinalOrder_thenMoveToCaseProgressionState() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setIsFinalOrder(NO);
            caseData.setObligationDatePresent(NO);

            CaseDetails caseDetails = CaseDetails.builder()
                .state(CaseState.DECISION_OUTCOME.toString())
                .caseTypeId("CIVIL")
                .data(caseData.toMap(objectMapper)).build();

            CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
            CallbackParams params = callbackParamsOf(callbackRequest.getCaseDetails().getData(), caseData, ABOUT_TO_SUBMIT, null, CaseState.DECISION_OUTCOME);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getState()).isEqualTo(CaseState.CASE_PROGRESSION.name());
        }

        @Test
        void shouldSetStoredObligationData_whenObligationDataIsPresent() {
            LocalDateTime localDateTime = LocalDateTime.of(2024, 01, 01, 10, 10, 10);
            Mockito.when(time.now()).thenReturn(localDateTime);
            Mockito.when(userService.getUserDetails(any())).thenReturn(UserDetails
                                                                           .builder()
                                                                           .forename("John")
                                                                           .surname("Smith")
                                                                           .build());

            LocalDate obligationDate = LocalDate.of(2024, 12, 12);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setIsFinalOrder(YesOrNo.YES);
            ObligationData obligationData = new ObligationData();
            obligationData.setObligationReason(ObligationReason.STAY_A_CASE);
            obligationData.setObligationDate(obligationDate);
            obligationData.setObligationAction("Main text");
            UUID uuid = UUID.fromString("818da749-8920-40c2-a083-722645735e02");
            Element<ObligationData> element = new Element<>();
            element.setId(uuid);
            element.setValue(obligationData);
            caseData.setObligationData(List.of(element));

            StoredObligationData expectedData = new StoredObligationData();
            expectedData.setCreatedBy("John Smith");
            expectedData.setCreatedOn(time.now());
            expectedData.setObligationDate(obligationDate);
            expectedData.setObligationAction("Main text");
            expectedData.setObligationReason(ObligationReason.STAY_A_CASE);
            expectedData.setReasonText(ObligationReason.STAY_A_CASE.getDisplayedValue());
            expectedData.setObligationWATaskRaised(NO);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(data.getStoredObligationData().get(0).getValue()).isEqualTo(expectedData);
        }

        @Test
        void shouldSetStoredObligationData_whenObligationDataIsPresent_withOtherReason() {
            LocalDateTime localDateTime = LocalDateTime.of(2024, 01, 01, 10, 10, 10);
            Mockito.when(time.now()).thenReturn(localDateTime);
            Mockito.when(userService.getUserDetails(any())).thenReturn(UserDetails
                                                                           .builder()
                                                                           .forename("John")
                                                                           .surname("Smith")
                                                                           .build());

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setIsFinalOrder(YesOrNo.YES);
            ObligationData obligationData = new ObligationData();
            obligationData.setObligationReason(ObligationReason.OTHER);
            obligationData.setOtherObligationReason("Reason for othering");
            LocalDate obligationDate = LocalDate.of(2024, 12, 12);
            obligationData.setObligationDate(obligationDate);
            obligationData.setObligationAction("Main text");
            UUID uuid = UUID.fromString("818da749-8920-40c2-a083-722645735e02");
            Element<ObligationData> element = new Element<>();
            element.setId(uuid);
            element.setValue(obligationData);
            caseData.setObligationData(List.of(element));

            StoredObligationData expectedData = new StoredObligationData();
            expectedData.setCreatedBy("John Smith");
            expectedData.setCreatedOn(time.now());
            expectedData.setObligationDate(obligationDate);
            expectedData.setObligationAction("Main text");
            expectedData.setObligationReason(ObligationReason.OTHER);
            expectedData.setOtherObligationReason("Reason for othering");
            expectedData.setReasonText(ObligationReason.OTHER
                                .getDisplayedValue() + ": Reason for othering");
            expectedData.setObligationWATaskRaised(NO);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(data.getStoredObligationData().get(0).getValue()).isEqualTo(expectedData);
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnConfirmationBodyInResponse_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted()
                .build();
            caseData.setObligationDatePresent(NO);
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
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted()
                .build();
            caseData.setObligationDatePresent(YesOrNo.YES);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualTo(SubmittedCallbackResponse
                                               .builder()
                                               .confirmationHeader(HEADER_CONFIRMATION)
                                               .confirmationBody(BODY_CONFIRMATION_OBLIGATION)
                                               .build());
        }

    }
}
