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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
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

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mapper.registerModules(new JavaTimeModule(), new Jdk8Module());
        handler = new ManageStayCallbackHandler(
            featureToggleService,
            mapper
        );
    }

    @Nested
    class AboutToStart {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateDecisionOutcome().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldSetManageStayOptionToNull_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateDecisionOutcome().build();
            caseData.toBuilder().manageStayOption("LIFT_STAY").build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

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

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldReturnNoError_WhenAboutToSubmitIsInvokedToggleTrue() {
            CaseState preStayState = CaseState.JUDICIAL_REFERRAL;
            CaseData caseData = CaseDataBuilder.builder().atStateDecisionOutcome().build()
                .toBuilder().manageStayOption("LIFT_STAY").ccdState(preStayState)
                .preStayState(preStayState.name()).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

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
        void shouldSetCorrectCaseState_WhenManageStayOptionIsLiftStay(CaseState preStayState, CaseState expectedState) {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .manageStayOption("LIFT_STAY").ccdState(preStayState)
                .preStayState(preStayState.name()).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getState()).isEqualTo(expectedState.name());
        }

        @Test
        void shouldNotChangeCaseState_WhenManageStayOptionIsNotLiftStay() {

            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .manageStayOption("REQUEST_UPDATE").ccdState(CASE_STAYED).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

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
                Arguments.of(CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING, CaseState.CASE_PROGRESSION)
            );
        }
    }

    @Nested
    class Submitted {

        @Test
        void shouldReturnNoError_WhenSubmittedIsInvoked() {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateDecisionOutcome().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseDetails).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvokedWithLiftStay() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .manageStayOption("LIFT_STAY").build();
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader("# You have lifted the stay from this \n\n # case \n\n ## All parties have been notified")
                    .confirmationBody("&nbsp;")
                    .build());
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvokedWithRequestUpdate() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .manageStayOption("REQUEST_UPDATE").build();
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader("# You have requested an update on \n\n # this case \n\n ## All parties have been notified")
                    .confirmationBody("&nbsp;")
                    .build());
        }
    }
}
