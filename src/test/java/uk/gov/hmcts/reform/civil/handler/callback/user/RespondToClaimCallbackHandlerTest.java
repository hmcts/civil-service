package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.sampledata.AddressBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.APPLICANT_RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RespondToClaimCallbackHandler.class,
    ExitSurveyConfiguration.class,
    ExitSurveyContentService.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    DateOfBirthValidator.class,
    UnavailableDateValidator.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class
})
class RespondToClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private Time time;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @Autowired
    private RespondToClaimCallbackHandler handler;

    @Autowired
    private ExitSurveyContentService exitSurveyContentService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @MockBean
    private StateFlowEngine stateFlowEngine;

    @Mock
    private StateFlow mockedStateFlow;

    @Autowired
    private UserService userService;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldPopulateRespondentCopies_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondent2(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("respondent1Copy")).isEqualTo(response.getData().get("respondent1"));
            assertThat(response.getData().get("respondent2Copy")).isEqualTo(response.getData().get("respondent2"));
        }

        @Test
        void shouldNotError_WhenNoRespondent2() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("respondent1Copy")).isEqualTo(response.getData().get("respondent1"));
        }
    }

    @Nested
    class AboutToStartCallbackV1 {

        @BeforeEach
        public void setup() {
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        }

        @Test
        void shouldPopulateRespondentCopies_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondent2(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("respondent1Copy")).isEqualTo(response.getData().get("respondent1"));
            assertThat(response.getData().get("respondent2Copy")).isEqualTo(response.getData().get("respondent2"));
        }

        @Test
        void shouldNotError_WhenNoRespondent2() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("respondent1Copy")).isEqualTo(response.getData().get("respondent1"));
            assertThat(response.getData().get("respondent2Copy")).isNull();
            assertThat(response.getData().get("respondent2Copy")).isNull();
        }

        @Test
        void shouldTriggerError_WhenRespondent1HasAlreadySubmittedResponseAndTheyTryToSubmitAgain() {
            when(featureToggleService.isMultipartyEnabled()).thenReturn(true);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting2ndRespondentResponse()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()
                           .equals("There is a problem\nYou have already submitted the defendant's response"));
        }

        @Test
        void shouldTriggerError_WhenRespondent2HasAlreadySubmittedResponseAndTheyTryToSubmitAgain() {
            when(featureToggleService.isMultipartyEnabled()).thenReturn(true);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting1stRespondentResponse()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()
                           .equals("There is a problem\nYou have already submitted the defendant's response"));
        }

        @Test
        void shouldTriggerError_WhenRespondent1TriesToSubmitResponseAfterDeadline() {
            when(featureToggleService.isMultipartyEnabled()).thenReturn(true);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting1stRespondentResponse()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(1))
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors())
                .containsExactly("You cannot submit a response now as you have passed your deadline");
        }

        @Test
        void shouldTriggerError_WhenRespondent2TriesToSubmitResponseAfterDeadline() {
            when(featureToggleService.isMultipartyEnabled()).thenReturn(true);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(false);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting2ndRespondentResponse()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent2ResponseDeadline(LocalDateTime.now().minusDays(1))
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors())
                .containsExactly("You cannot submit a response now as you have passed your deadline");
        }

        @Test
        void shouldNotTriggerError_WhenRespondent1TriesToSubmitResponseBeforeDeadline() {
            when(featureToggleService.isMultipartyEnabled()).thenReturn(true);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting1stRespondentResponse()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent2ResponseDeadline(LocalDateTime.now().plusDays(1))
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldNotTriggerError_WhenRespondent2TriesToSubmitResponseBeforeDeadline() {
            when(featureToggleService.isMultipartyEnabled()).thenReturn(true);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(false);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting2ndRespondentResponse()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent2ResponseDeadline(LocalDateTime.now().plusDays(1))
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class MidEventConfirmDetailsCallback {

        private static final String PAGE_ID = "confirm-details";

        @Test
        void shouldReturnError_whenIndividualDateOfBirthIsInTheFuture() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(PartyBuilder.builder().individual()
                                 .individualDateOfBirth(LocalDate.now().plusDays(1))
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("The date entered cannot be in the future");
        }

        @Test
        void shouldReturnError_whenSoleTraderDateOfBirthIsInTheFuture() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(PartyBuilder.builder().individual()
                                 .soleTraderDateOfBirth(LocalDate.now().plusDays(1))
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("The date entered cannot be in the future");
        }

        @Test
        void shouldReturnNoError_whenIndividualDateOfBirthIsInThePast() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(PartyBuilder.builder().individual()
                                 .individualDateOfBirth(LocalDate.now().minusYears(1))
                                 .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenSoleTraderDateOfBirthIsInThePast() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(PartyBuilder.builder().individual()
                                 .soleTraderDateOfBirth(LocalDate.now().minusYears(1))
                                 .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidEventCallbackValidateUnavailableDates {

        private static final String PAGE_ID = "validate-unavailable-dates";

        @BeforeEach
        public void setup() {
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        }

        @Test
        void shouldValidateExperts_whenMultipartyAndSolicitorRepresentsOnlyOneOfRespondentsAndResSolTwoRole() {
            when(featureToggleService.isMultipartyEnabled()).thenReturn(true);
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(RESPONDENTSOLICITORTWO)))
                .thenReturn(true);
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(wrapElements(UnavailableDate.builder().date(now().plusDays(5)).build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(hearing).build())
                .respondent2DQ(Respondent2DQ.builder().respondent2DQHearing(hearing).build())
                .build().toBuilder().ccdCaseReference(1234L)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldValidateExperts_whenDef2HasSameLRAndDiffResAndRespondent2DQHearingPresent() {
            when(featureToggleService.isMultipartyEnabled()).thenReturn(true);
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(RESPONDENTSOLICITORTWO)))
                .thenReturn(false);
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(wrapElements(UnavailableDate.builder().date(now().plusDays(5)).build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(hearing).build())
                .respondent2DQ(Respondent2DQ.builder().respondent2DQHearing(hearing).build())
                .build().toBuilder().ccdCaseReference(1234L)
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_whenUnavailableDateIsMoreThanOneYearInFuture() {
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(wrapElements(UnavailableDate.builder().date(now().plusYears(5)).build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(hearing).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors())
                .containsExactly("The date cannot be in the past and must not be more than a year in the future");
        }

        @Test
        void shouldReturnError_whenUnavailableDateIsInPast() {
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(wrapElements(UnavailableDate.builder().date(now().minusYears(5)).build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(hearing).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors())
                .containsExactly("The date cannot be in the past and must not be more than a year in the future");
        }

        @Test
        void shouldReturnNoError_whenUnavailableDateIsValid() {
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(wrapElements(UnavailableDate.builder().date(now().plusDays(5)).build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(hearing).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenNoUnavailableDate() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(Hearing.builder().build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenUnavailableDatesNotRequired() {
            Hearing hearing = Hearing.builder().unavailableDatesRequired(NO).build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(hearing).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidEventCallbackValidateExperts {

        private static final String PAGE_ID = "experts";

        @BeforeEach
        public void setup() {
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        }

        @Test
        void shouldValidateExperts_whenMultipartyAndSolicitorRepresentsOnlyOneOfRespondentsAndResSolOneRole() {
            when(featureToggleService.isMultipartyEnabled()).thenReturn(true);
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(RESPONDENTSOLICITORONE)))
                .thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ
                                   .builder().respondent1DQExperts(Experts.builder()
                                                                       .expertRequired(NO)
                                                                       .build()).build())
                .build().toBuilder().ccdCaseReference(1234L)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldValidateExperts_whenMultipartyAndSolicitorRepresentsOnlyOneOfRespondentsAndResSolTwoRole() {
            when(featureToggleService.isMultipartyEnabled()).thenReturn(true);
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
                .thenReturn(false).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .respondent2DQ(Respondent2DQ
                                   .builder().respondent2DQExperts(Experts.builder().expertRequired(NO).build())
                                   .build())
                .build().toBuilder().ccdCaseReference(1234L)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldValidateExperts_whenMultipartyAndSameLRDiffResponseAndRespondent2DQExperts() {
            when(featureToggleService.isMultipartyEnabled()).thenReturn(true);
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
                .thenReturn(false).thenReturn(false);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent2DQ(Respondent2DQ
                                   .builder().respondent2DQExperts(Experts.builder().expertRequired(NO).build())
                                   .build())
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_whenExpertRequiredAndNullDetails() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQExperts(Experts.builder()
                                                             .expertRequired(YES)
                                                             .build())
                                   .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("Expert details required");
        }

        @Test
        void shouldReturnNoError_whenExpertRequiredAndDetailsProvided() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQExperts(Experts.builder()
                                                             .expertRequired(YES)
                                                             .details(wrapElements(Expert.builder()
                                                                                       .name("test expert").build()))
                                                             .build())
                                   .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenExpertNotRequired() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQExperts(Experts.builder()
                                                             .expertRequired(NO)
                                                             .build())
                                   .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidEventCallbackValidateWitnesses {

        private static final String PAGE_ID = "witnesses";

        @BeforeEach
        public void setup() {
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        }

        @Test
        void shouldValidateWitness_whenMultipartyAndSolicitorRepresentsOnlyOneOfRespondentsAndResSolOneRole() {
            when(featureToggleService.isMultipartyEnabled()).thenReturn(true);
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(RESPONDENTSOLICITORONE)))
                .thenReturn(true);
            Witnesses witnesses = Witnesses.builder().witnessesToAppear(YES).build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQWitnesses(witnesses).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("Witness details required");
        }

        @Test
        void shouldValidateWitness_whenMultipartyAndSolicitorRepresentsOnlyOneOfRespondentsAndResSolTwoRole() {
            when(featureToggleService.isMultipartyEnabled()).thenReturn(true);
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
                .thenReturn(false).thenReturn(true);
            Witnesses witnesses = Witnesses.builder().witnessesToAppear(YES).build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent2DQ(Respondent2DQ.builder().respondent2DQWitnesses(witnesses).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("Witness details required");
        }

        @Test
        void shouldValidateWitness_whenMultipartyAndSameLRDiffResponseAndRespondent2DQWitness() {
            when(featureToggleService.isMultipartyEnabled()).thenReturn(true);
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
                .thenReturn(false).thenReturn(false);
            Witnesses witnesses = Witnesses.builder().witnessesToAppear(YES).build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent2DQ(Respondent2DQ.builder().respondent2DQWitnesses(witnesses).build())
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("Witness details required");
        }

        @Test
        void shouldReturnError_whenWitnessRequiredAndNullDetails() {
            when(featureToggleService.isMultipartyEnabled()).thenReturn(false);
            Witnesses witnesses = Witnesses.builder().witnessesToAppear(YES).build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQWitnesses(witnesses).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("Witness details required");
        }

        @Test
        void shouldReturnNoError_whenWitnessRequiredAndDetailsProvided() {
            when(featureToggleService.isMultipartyEnabled()).thenReturn(false);
            List<Element<Witness>> testWitness = wrapElements(Witness.builder().name("test witness").build());
            Witnesses witnesses = Witnesses.builder().witnessesToAppear(YES).details(testWitness).build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQWitnesses(witnesses).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenWitnessNotRequired() {
            when(featureToggleService.isMultipartyEnabled()).thenReturn(false);
            Witnesses witnesses = Witnesses.builder().witnessesToAppear(NO).build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQWitnesses(witnesses).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidStatementOfTruth {

        @Test
        void shouldSetStatementOfTruthFieldsToNull_whenPopulated() {
            String name = "John Smith";
            String role = "Solicitor";

            CaseData caseData = CaseDataBuilder.builder()
                .uiStatementOfTruth(StatementOfTruth.builder().name(name).role(role).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, "statement-of-truth");
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("uiStatementOfTruth")
                .extracting("name", "role")
                .containsExactly(null, null);
        }
    }

    @Nested
    class AboutToSubmitCallback {
        LocalDateTime responseDate = LocalDateTime.now();
        LocalDateTime deadline = LocalDateTime.now().plusDays(4);

        @BeforeEach
        void setup() {
            when(time.now()).thenReturn(responseDate);
            when(deadlinesCalculator.calculateApplicantResponseDeadline(
                any(LocalDateTime.class),
                any(AllocatedTrack.class)
            )).thenReturn(deadline);
        }

        @Test
        void shouldSetApplicantResponseDeadline_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME));
        }

        @Test
        void shouldUpdateBusinessProcess_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(DEFENDANT_RESPONSE.name());

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
        }

        @Test
        void shouldCopyRespondent1PrimaryAddress_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .build();

            var expectedAddress = AddressBuilder.defaults().addressLine1("test address").build();
            caseData = caseData.toBuilder()
                .respondent1Copy(caseData.getRespondent1().toBuilder().primaryAddress(expectedAddress).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).doesNotContainKey("respondent1Copy");
            assertThat(response.getData())
                .extracting("respondent1").extracting("primaryAddress").extracting("AddressLine1")
                .isEqualTo("test address");
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndBusinessProcess_when1ResponseAndNotMultiparty() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Nested
        class ResetStatementOfTruth {

            @Test
            void shouldMoveStatementOfTruthToCorrectFieldAndResetUIField_whenInvoked() {
                String name = "John Smith";
                String role = "Solicitor";

                CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build()
                    .toBuilder()
                    .respondent1Copy(PartyBuilder.builder().individual().build())
                    .uiStatementOfTruth(StatementOfTruth.builder().name(name).role(role).build())
                    .build();
                CallbackParams callbackParams = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                assertThat(response.getData())
                    .extracting("respondent1DQStatementOfTruth")
                    .extracting("name", "role")
                    .containsExactly(name, role);

                assertThat(response.getData())
                    .extracting("uiStatementOfTruth")
                    .extracting("name", "role")
                    .containsExactly(null, null);
            }
        }
    }

    @Nested
    class AboutToSubmitCallbackV1 {
        LocalDateTime responseDate = LocalDateTime.now();
        LocalDateTime deadline = LocalDateTime.now().plusDays(4);

        @BeforeEach
        void setup() {
            when(time.now()).thenReturn(responseDate);
            when(deadlinesCalculator.calculateApplicantResponseDeadline(
                any(LocalDateTime.class),
                any(AllocatedTrack.class)
            )).thenReturn(deadline);

            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        }

        @Test
        void shouldCopyRespondent1ResponseSetApplicantResponseAndSetBusinessProcess_whenOneRepGivingOneAnswer() {
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                .respondentResponseIsSame(YES)
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry("respondent2ClaimResponseType", caseData.getRespondent1ClaimResponseType().toString())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME))
                .containsEntry("respondent2ResponseDate", responseDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndSetBusinessProcess_when1v2SameSolicitorResponseIsNotTheSame() {
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .respondentResponseIsSame(NO)
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME))
                .containsEntry("respondent2ResponseDate", responseDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndSetBusinessProcess_when1v2SameSolicitorResponseIsTheSame() {
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                //respondent1ClaimResponseType is copied into respondent2ClaimResponseType via handler
                .atStateRespondentFullDefence()
                .respondentResponseIsSame(YES)
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME))
                .containsEntry("respondent2ResponseDate", responseDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndSetBusinessProcess_when2ndRepAnsweringAfter1stAlreadyAnswered() {
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                .respondent2Responds(FULL_DEFENCE)
                .respondent2DQ()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("respondent1ResponseDate", caseData.getRespondent1ResponseDate().format(ISO_DATE_TIME))
                .containsEntry("respondent2ResponseDate", responseDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndSetBusinessProcess_when1stRepAnsweringAfter2ndAlreadyAnswered() {
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                .respondent2Responds(FULL_DEFENCE)
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME))
                .containsEntry("respondent2ResponseDate", caseData.getRespondent2ResponseDate().format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Test
        void shouldNotSetApplicantResponseDeadlineOrTransitionCcdState_when1stRespondentAnsweringBefore2nd() {
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting2ndRespondentResponse()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("applicant1ResponseDeadline").isNull();
            assertThat(response.getData()).extracting("respondent1ResponseDate")
                .isEqualTo(responseDate.format(ISO_DATE_TIME));
            assertThat(response.getData()).extracting("respondent2ResponseDate").isNull();
            assertEquals(response.getState(), CaseState.AWAITING_APPLICANT_INTENTION.name());
        }

        @Test
        void shouldNotSetApplicantResponseDeadlineOrTransitionCcdState_when2ndRespondentAnsweringBefore1st() {
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting1stRespondentResponse()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("applicant1ResponseDeadline").isNull();
            assertThat(response.getData()).extracting("respondent2ResponseDate")
                .isEqualTo(responseDate.format(ISO_DATE_TIME));
            assertThat(response.getData()).extracting("respondent1ResponseDate").isNull();
            assertEquals(response.getState(), CaseState.AWAITING_APPLICANT_INTENTION.name());
        }

        @Test
        void shouldCopyRespondentPrimaryAddresses_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                .respondent2(PartyBuilder.builder().individual().build())
                .build();
            String address = "test address";
            var expectedAddress = AddressBuilder.defaults().addressLine1(address).build();
            caseData = caseData.toBuilder()
                .respondent1Copy(caseData.getRespondent1().toBuilder().primaryAddress(expectedAddress).build())
                .respondent2Copy(caseData.getRespondent2().toBuilder().primaryAddress(expectedAddress).build())
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).doesNotContainKey("respondent1Copy");
            assertThat(response.getData())
                .extracting("respondent1").extracting("primaryAddress").extracting("AddressLine1").isEqualTo(address);
            assertThat(response.getData())
                .extracting("respondent2").extracting("primaryAddress").extracting("AddressLine1").isEqualTo(address);
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndBusinessProcess_when1ResponseAndNotMultiparty() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndSetBusinessProcess_whenOneDefendantRepAnsweringToTwoApplicants() {
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoApplicants()
                .atStateClaimDetailsNotified()
                .respondent1ClaimResponseTypeToApplicant1(FULL_DEFENCE)
                .respondent1ClaimResponseTypeToApplicant2(FULL_DEFENCE)
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME));

            assertThat(response.getData()).extracting("respondent2ResponseDate").isNull();
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Nested
        class ResetStatementOfTruth {

            @Test
            void shouldMoveStatementOfTruthToCorrectFieldAndResetUIField_when1V1() {
                String name = "John Smith";
                String role = "Solicitor";

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                    .build()
                    .toBuilder()
                    .respondent1Copy(PartyBuilder.builder().individual().build())
                    .uiStatementOfTruth(StatementOfTruth.builder().name(name).role(role).build())
                    .build();
                CallbackParams callbackParams = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                assertThat(response.getData())
                    .extracting("respondent1DQStatementOfTruth")
                    .extracting("name", "role")
                    .containsExactly(name, role);

                assertThat(response.getData())
                    .extracting("uiStatementOfTruth")
                    .extracting("name", "role")
                    .containsExactly(null, null);
            }

            @Test
            void shouldMoveStatementOfTruthToCorrectFieldAndResetUIField_when2V1SameRep() {
                String name = "John Smith";
                String role = "Solicitor";

                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoApplicants()
                    .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                    .build()
                    .toBuilder()
                    .respondent1Copy(PartyBuilder.builder().individual().build())
                    .uiStatementOfTruth(StatementOfTruth.builder().name(name).role(role).build())
                    .build();
                CallbackParams callbackParams = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                assertThat(response.getData())
                    .extracting("respondent1DQStatementOfTruth")
                    .extracting("name", "role")
                    .containsExactly(name, role);

                assertThat(response.getData())
                    .extracting("uiStatementOfTruth")
                    .extracting("name", "role")
                    .containsExactly(null, null);
            }
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnConfirmationScreen_when1v1ResponseSubmitted() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(
                        format("# You have submitted the Defendant's defence%n## Claim number: 000DC001"))
                    .confirmationBody(format(
                        "<br /> The Claimant legal representative will get a notification to confirm you have "
                            + "provided the Defendant defence. You will be CC'ed.%n"
                            + "The Claimant has until %s to discontinue or proceed with this claim",
                        formatLocalDateTime(APPLICANT_RESPONSE_DEADLINE, DATE))
                        + exitSurveyContentService.respondentSurvey())
                    .build());
        }

        @Test
        void shouldReturnConfirmationScreen_when1v2_andReceivedFirstResponse() {
            when(featureToggleService.isMultipartyEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(
                        format("# You have submitted the Defendant's defence%n## Claim number: 000DC001"))
                    .confirmationBody("Once the other defendant's legal representative has submitted their defence, "
                                          + "we will send the claimant's legal representative a notification. "
                                          + "You will receive a copy of this notification, as it will include details "
                                          + "of when the claimant must respond.")
                    .build());
        }

        @Test
        void shouldReturnConfirmationScreen_when1v2_andReceivedBothResponses() {
            when(featureToggleService.isMultipartyEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(
                        format("# You have submitted the Defendant's defence%n## Claim number: 000DC001"))
                    .confirmationBody(format(
                        "<br /> The Claimant legal representative will get a notification to confirm you have "
                            + "provided the Defendant defence. You will be CC'ed.%n"
                            + "The Claimant has until %s to discontinue or proceed with this claim",
                        formatLocalDateTime(APPLICANT_RESPONSE_DEADLINE, DATE)
                                      )
                                          + exitSurveyContentService.respondentSurvey())
                    .build());
        }
    }

    @Nested
    class MidEventSetGenericResponseTypeFlagCallback {

        private static final String PAGE_ID = "set-generic-response-type-flag";

        @Test
        void shouldSetMultiPartyResponseTypeFlags_Respondent1IsFullDefence() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags").isEqualTo("FULL_DEFENCE");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_Respondent2IsFullDefence() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
                .respondent1ClaimResponseType(null)
                .respondent2ClaimResponseType(FULL_DEFENCE)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags").isEqualTo("FULL_DEFENCE");
        }
    }
}
