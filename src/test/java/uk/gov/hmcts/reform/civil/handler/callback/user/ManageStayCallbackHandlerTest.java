package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_STAY;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STAY_LIFTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STAY_UPDATE_REQUESTED;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_STAYED;

@ExtendWith(MockitoExtension.class)
public class ManageStayCallbackHandlerTest {

    @InjectMocks
    private ManageStayCallbackHandler handler;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mapper.registerModules(new JavaTimeModule(), new Jdk8Module());
        handler = new ManageStayCallbackHandler(
            featureToggleService,
            deadlinesCalculator,
            mapper
        );
    }

    @Nested
    class AboutToStart {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateDecisionOutcome().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldSetManageStayOptionToNull_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateDecisionOutcome().build();
            caseData.setManageStayOption("LIFT_STAY");

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedCaseData.getManageStayOption()).isNull();
        }
    }

    @Nested
    class AboutToSubmit {

        @Test
        void shouldReturnNoError_WhenAboutToSubmitIsInvokedToggleFalse() {
            CaseData caseData = CaseDataBuilder.builder().atStateDecisionOutcome().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldReturnNoError_WhenAboutToSubmitIsInvokedToggleTrue() {
            CaseState preStayState = CaseState.JUDICIAL_REFERRAL;
            CaseData caseData = CaseDataBuilder.builder().atStateDecisionOutcome().build();
            caseData.setManageStayOption("LIFT_STAY");
            caseData.setCcdState(preStayState);
            caseData.setPreStayState(preStayState.name());

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("businessProcess")
                .extracting("status", "camundaEvent")
                .containsExactly(READY.name(), STAY_LIFTED.name());
        }

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents()).contains(MANAGE_STAY);
        }

        @ParameterizedTest
        @MethodSource("provideCaseStatesForLiftStay")
        void shouldSetCorrectCaseState_WhenManageStayOptionIsLiftStay(
            CaseState preStayState,
            CaseState expectedState
        ) {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            caseData.setManageStayOption("LIFT_STAY");
            caseData.setCcdState(preStayState);
            caseData.setPreStayState(preStayState.name());

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getState()).isEqualTo(expectedState.name());
        }

        @Test
        void shouldNotChangeCaseState_WhenManageStayOptionIsNotLiftStay() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            caseData.setManageStayOption("REQUEST_UPDATE");
            caseData.setCcdState(CASE_STAYED);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getState()).isEqualTo(CASE_STAYED.name());
            assertThat(response.getData()).extracting("businessProcess")
                .extracting("status", "camundaEvent")
                .containsExactly(READY.name(), STAY_UPDATE_REQUESTED.name());
        }

        private static Stream<Arguments> provideCaseStatesForLiftStay() {
            return Stream.of(
                Arguments.of(CaseState.IN_MEDIATION, CaseState.JUDICIAL_REFERRAL),
                Arguments.of(CaseState.JUDICIAL_REFERRAL, CaseState.JUDICIAL_REFERRAL),
                Arguments.of(CaseState.CASE_PROGRESSION, CaseState.CASE_PROGRESSION),
                Arguments.of(CaseState.HEARING_READINESS, CaseState.CASE_PROGRESSION),
                Arguments.of(CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING, CaseState.CASE_PROGRESSION),
                Arguments.of(CaseState.DECISION_OUTCOME, CaseState.CASE_PROGRESSION),
                Arguments.of(CaseState.All_FINAL_ORDERS_ISSUED, CaseState.CASE_PROGRESSION),
                Arguments.of(CaseState.AWAITING_APPLICANT_INTENTION, CaseState.AWAITING_APPLICANT_INTENTION),
                Arguments.of(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT, CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT)
            );
        }
    }

    @Nested
    class Submitted {

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvokedWithLiftStay() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            caseData.setManageStayOption("LIFT_STAY");

            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            SubmittedCallbackResponse response =
                (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader("# You have lifted the stay from this \n\n # case \n\n ## All parties have been notified")
                    .confirmationBody("&nbsp;")
                    .build());
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvokedWithRequestUpdate() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            caseData.setManageStayOption("REQUEST_UPDATE");

            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            SubmittedCallbackResponse response =
                (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader("# You have requested an update on \n\n # this case \n\n ## All parties have been notified")
                    .confirmationBody("&nbsp;")
                    .build());
        }
    }

    @Test
    void shouldUpdateRespondentDeadlines_whenLiftingStayAndStateIsAwaitingRespondentAcknowledgement() {
        CaseData caseData = CaseDataBuilder.builder().build();

        caseData.setManageStayOption("LIFT_STAY");
        caseData.setPreStayState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT.name());
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);

        caseData.setCaseStayDate(LocalDate.now().minusDays(5));
        caseData.setRespondent1ResponseDeadline(LocalDate.now().plusDays(10).atStartOfDay());
        caseData.setRespondent2ResponseDeadline(LocalDate.now().plusDays(15).atStartOfDay());

        when(deadlinesCalculator.plusDaysAt4pmDeadline(any(), eq(5L)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        CallbackParams params = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CaseData updated =
            mapper.convertValue(response.getData(), CaseData.class);

        verify(deadlinesCalculator, times(2))
            .plusDaysAt4pmDeadline(any(), eq(5L));

        assertThat(updated.getRespondent1ResponseDeadline()).isNotNull();
        assertThat(updated.getRespondent2ResponseDeadline()).isNotNull();
    }

    @Test
    void shouldUpdateApplicantDeadline_whenLiftingStayAndStateIsAwaitingApplicantIntention() {
        CaseData caseData = CaseDataBuilder.builder().build();

        caseData.setManageStayOption("LIFT_STAY");
        caseData.setPreStayState(CaseState.AWAITING_APPLICANT_INTENTION.name());
        caseData.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);

        caseData.setCaseStayDate(LocalDate.now().minusDays(3));
        caseData.setApplicant1ResponseDeadline(LocalDate.now().plusDays(7).atStartOfDay());

        when(deadlinesCalculator.plusDaysAt4pmDeadline(any(), eq(3L)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        CallbackParams params = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CaseData updated =
            mapper.convertValue(response.getData(), CaseData.class);

        verify(deadlinesCalculator)
            .plusDaysAt4pmDeadline(any(), eq(3L));

        assertThat(updated.getApplicant1ResponseDeadline()).isNotNull();
    }

    @Test
    void shouldNotUpdateDeadlines_whenCaseStayDateIsNull() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setManageStayOption("LIFT_STAY");
        caseData.setPreStayState(CaseState.AWAITING_APPLICANT_INTENTION.name());

        CallbackParams params = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(params);

        verifyNoInteractions(deadlinesCalculator);
    }
}
