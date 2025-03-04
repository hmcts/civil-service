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

import java.time.LocalDate;
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
                .obligationDatePresent(NO)
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
                .obligationDatePresent(NO)
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
        void shouldSetAllFinalOrdersIssuedState_whenIsFinalOrder() {
            CaseData caseData = CaseData.builder()
                .isFinalOrder(YesOrNo.YES)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getState()).isEqualTo(CaseState.All_FINAL_ORDERS_ISSUED.name());
        }

        @Test
        void shouldSetStoredObligationData_whenObligationDataIsPresent() {
            LocalDateTime localDateTime = LocalDateTime.of(2024, 01, 01, 10, 10, 10);
            Mockito.when(time.now()).thenReturn(localDateTime);
            UUID uuid = UUID.fromString("818da749-8920-40c2-a083-722645735e02");
            Mockito.when(userService.getUserDetails(any())).thenReturn(UserDetails
                                                                           .builder()
                                                                           .forename("John")
                                                                           .surname("Smith")
                                                                           .build());

            LocalDate obligationDate = LocalDate.of(2024, 12, 12);
            CaseData caseData = CaseData.builder()
                .isFinalOrder(YesOrNo.YES)
                .obligationData(List.of(Element.<ObligationData>builder()
                                            .id(uuid)
                                            .value(ObligationData.builder()
                                                       .obligationReason(ObligationReason.STAY_A_CASE)
                                                       .obligationDate(obligationDate)
                                                       .obligationAction("Main text")
                                                       .build()
                                            )
                                            .build()
                                )
                )
                .build();

            StoredObligationData expectedData = StoredObligationData
                                                  .builder()
                                                  .createdBy("John Smith")
                                                  .createdOn(time.now())
                                                  .obligationDate(obligationDate)
                                                  .obligationAction("Main text")
                                                  .obligationReason(ObligationReason.STAY_A_CASE)
                                                  .reasonText(ObligationReason.STAY_A_CASE
                                                                  .getDisplayedValue())
                                                  .obligationWATaskRaised(NO)
                                                  .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(data.getStoredObligationData().get(0).getValue()).isEqualTo(expectedData);
        }

        @Test
        void shouldSetStoredObligationData_whenObligationDataIsPresent_withOtherReason() {
            LocalDateTime localDateTime = LocalDateTime.of(2024, 01, 01, 10, 10, 10);
            Mockito.when(time.now()).thenReturn(localDateTime);
            UUID uuid = UUID.fromString("818da749-8920-40c2-a083-722645735e02");
            Mockito.when(userService.getUserDetails(any())).thenReturn(UserDetails
                                                                           .builder()
                                                                           .forename("John")
                                                                           .surname("Smith")
                                                                           .build());

            LocalDate obligationDate = LocalDate.of(2024, 12, 12);
            CaseData caseData = CaseData.builder()
                .isFinalOrder(YesOrNo.YES)
                .obligationData(List.of(Element.<ObligationData>builder()
                                            .id(uuid)
                                            .value(ObligationData.builder()
                                                       .obligationReason(ObligationReason.OTHER)
                                                       .otherObligationReason("Reason for othering")
                                                       .obligationDate(obligationDate)
                                                       .obligationAction("Main text")
                                                       .build()
                                            )
                                            .build()
                                )
                )
                .build();

            StoredObligationData expectedData = StoredObligationData
                .builder()
                .createdBy("John Smith")
                .createdOn(time.now())
                .obligationDate(obligationDate)
                .obligationAction("Main text")
                .obligationReason(ObligationReason.OTHER)
                .otherObligationReason("Reason for othering")
                .reasonText(ObligationReason.OTHER
                                .getDisplayedValue() + ": Reason for othering")
                .obligationWATaskRaised(NO)
                .build();

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
            caseData.builder().obligationDatePresent(NO).build();
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
            caseData = caseData.builder().obligationDatePresent(YesOrNo.YES).build();
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
