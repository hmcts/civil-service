package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.AddressBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.APPLICANT_RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.RESPONSE_DEADLINE;
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
    LocationRefDataService.class,
    CourtLocationUtils.class,
    StateFlowEngine.class,
    AssignCategoryId.class
})
class RespondToClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Time time;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @Autowired
    private RespondToClaimCallbackHandler handler;

    @Autowired
    private ExitSurveyContentService exitSurveyContentService;

    @Autowired
    private AssignCategoryId assignCategoryId;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @MockBean
    private LocationRefDataService locationRefDataService;

    @MockBean
    private CourtLocationUtils courtLocationUtils;

    @MockBean
    private StateFlowEngine stateFlowEngine;

    @Mock
    private StateFlow mockedStateFlow;

    @Autowired
    private UserService userService;

    @MockBean
    private CaseFlagsInitialiser caseFlagInitialiser;

    @Nested
    class AboutToStartCallback {

        @BeforeEach
        public void setup() {
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        }

        @Test
        void shouldPopulateRespondentCopies_WhenAboutToStartIsInvoked() {
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondent2(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //Then
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("respondent1Copy")).isEqualTo(response.getData().get("respondent1"));
            assertThat(response.getData().get("respondent2Copy")).isEqualTo(response.getData().get("respondent2"));
        }

        @Test
        void shouldNotError_WhenNoRespondent2() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //Then
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("respondent1Copy")).isEqualTo(response.getData().get("respondent1"));
            assertThat(response.getData().get("respondent2Copy")).isNull();
            assertThat(response.getData().get("respondent2Copy")).isNull();
        }

        @Test
        void shouldTriggerError_WhenRespondent1HasAlreadySubmittedResponseAndTheyTryToSubmitAgain() {
            //Given
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting2ndRespondentResponse()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //Then
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).contains("There is a problem\nYou have already submitted the defendant's response");
        }

        @Test
        void shouldTriggerError_WhenRespondent2HasAlreadySubmittedResponseAndTheyTryToSubmitAgain() {
            //Given
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting1stRespondentResponse()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //Then
            assertThat(response.getErrors()).isNotNull();
        }

        @Test
        void shouldTriggerError_WhenRespondent1TriesToSubmitResponseAfterDeadline() {
            //Given
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting1stRespondentResponse()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(1))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //Then
            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors())
                .containsExactly("You cannot submit a response now as you have passed your deadline");
        }

        @Test
        void shouldTriggerError_WhenRespondent2TriesToSubmitResponseAfterDeadline() {
            //Given
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(false);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting2ndRespondentResponse()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent2ResponseDeadline(LocalDateTime.now().minusDays(1))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //Then
            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors())
                .containsExactly("You cannot submit a response now as you have passed your deadline");
        }

        @Test
        void shouldNotTriggerError_WhenRespondent1TriesToSubmitResponseBeforeDeadline() {
            //Given
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting1stRespondentResponse()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent2ResponseDeadline(LocalDateTime.now().plusDays(1))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //Then
            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldNotTriggerError_WhenRespondent2TriesToSubmitResponseBeforeDeadline() {
            //Given
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(false);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting2ndRespondentResponse()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent2ResponseDeadline(LocalDateTime.now().plusDays(1))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //Then
            assertThat(response.getErrors()).isNull();
        }

        @Nested
        class CourtLocation {
            @Test
            void shouldHandleCourtLocationData() {
                //Given
                when(locationRefDataService.getCourtLocationsForDefaultJudgments(anyString()))
                    .thenReturn(Collections.singletonList(
                        LocationRefData.builder()
                            .courtLocationCode("123")
                            .siteName("Site name")
                            .build()
                    ));
                when(courtLocationUtils.getLocationsFromList(any()))
                    .thenReturn(fromList(List.of("Site 1 - Lane 1 - 123", "Site 2 - Lane 2 - 124")));

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified()
                    .build().toBuilder()
                    .courtLocation(uk.gov.hmcts.reform.civil.model.CourtLocation.builder()
                                       .applicantPreferredCourt("123")
                                       .build())
                    .build();

                CallbackParams callbackParams = callbackParamsOf(caseData, ABOUT_TO_START);
                //When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                RequestedCourt respondent1DQRequestedCourt = getCaseData(response)
                    .getRespondent1DQ().getRespondent1DQRequestedCourt();
                DynamicList dynamicList = respondent1DQRequestedCourt.getResponseCourtLocations();

                List<String> courtlist = dynamicList.getListItems().stream()
                    .map(DynamicListElement::getLabel)
                    .collect(Collectors.toList());
                //Then
                assertThat(courtlist).containsOnly("Site 1 - Lane 1 - 123", "Site 2 - Lane 2 - 124");
                assertThat(respondent1DQRequestedCourt.getOtherPartyPreferredSite())
                    .isEqualTo("123 Site name");
            }

            @Test
            void shouldHandleCourtLocationData_ForRespondent2() {
                //Given
                when(courtLocationUtils.getLocationsFromList(any()))
                    .thenReturn(fromList(List.of("Site 1 - Lane 1 - 123", "Site 2 - Lane 2 - 124")));

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified()
                    .multiPartyClaimOneDefendantSolicitor()
                    .build();

                CallbackParams callbackParams = callbackParamsOf(caseData, ABOUT_TO_START);
                //When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                DynamicList dynamicList = getCaseData(response)
                    .getRespondent2DQ().getRespondent2DQRequestedCourt().getResponseCourtLocations();

                List<String> courtlist = dynamicList.getListItems().stream()
                    .map(DynamicListElement::getLabel)
                    .collect(Collectors.toList());
                //Then
                assertThat(courtlist).containsOnly("Site 1 - Lane 1 - 123", "Site 2 - Lane 2 - 124");
            }

            private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
                return objectMapper.convertValue(response.getData(), CaseData.class);
            }
        }

    }

    @Nested
    class MidEventConfirmDetailsCallback {

        private static final String PAGE_ID = "confirm-details";

        @Test
        void shouldReturnError_whenIndividualDateOfBirthIsInTheFuture() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(PartyBuilder.builder().individual()
                                 .individualDateOfBirth(LocalDate.now().plusDays(1))
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getErrors()).containsExactly("The date entered cannot be in the future");
        }

        @Test
        void shouldReturnError_whenSoleTraderDateOfBirthIsInTheFuture() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(PartyBuilder.builder().individual()
                                 .soleTraderDateOfBirth(LocalDate.now().plusDays(1))
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getErrors()).containsExactly("The date entered cannot be in the future");
        }

        @Test
        void shouldReturnNoError_whenIndividualDateOfBirthIsInThePast() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(PartyBuilder.builder().individual()
                                 .individualDateOfBirth(LocalDate.now().minusYears(1))
                                 .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenSoleTraderDateOfBirthIsInThePast() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(PartyBuilder.builder().individual()
                                 .soleTraderDateOfBirth(LocalDate.now().minusYears(1))
                                 .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
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
            // Given
            given(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(RESPONDENTSOLICITORTWO)))
                .willReturn(true);
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(wrapElements(UnavailableDate.builder()
                                                   .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                   .date(now().plusDays(5)).build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(hearing).build())
                .respondent2DQ(Respondent2DQ.builder().respondent2DQHearing(hearing).build())
                .build().toBuilder().ccdCaseReference(1234L)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldValidateExperts_whenMultipartyAndSolicitorRepresentsOnlyOneOfRespondents() {
            // Given
            given(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(RESPONDENTSOLICITORTWO))).willReturn(true);
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(wrapElements(UnavailableDate.builder()
                                                   .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                   .date(now().plusDays(5)).build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(hearing).build())
                .respondent2DQ(Respondent2DQ.builder().respondent2DQHearing(hearing).build())
                .build().toBuilder().ccdCaseReference(1234L)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldValidateExperts_whenMultipartyAndRespondentHasSameLegalRepAndDiffResponse() {
            // Given
            given(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(RESPONDENTSOLICITORTWO)))
                .willReturn(false);
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(wrapElements(UnavailableDate.builder()
                                                   .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                   .date(now().plusDays(5)).build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(hearing).build())
                .respondent2DQ(Respondent2DQ.builder().respondent2DQHearing(hearing).build())
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(NO)
                .build().toBuilder().ccdCaseReference(1234L)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldValidateExperts_whenMultipartyAndRespondentHasDiffLegalRep() {
            // Given
            given(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(RESPONDENTSOLICITORTWO)))
                .willReturn(false);
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(wrapElements(UnavailableDate.builder()
                                                   .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                   .date(now().plusDays(5)).build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(hearing).build())
                .respondent2DQ(Respondent2DQ.builder().respondent2DQHearing(hearing).build())
                .respondent2SameLegalRepresentative(NO)
                .build().toBuilder().ccdCaseReference(1234L)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldValidateExperts_whenMultipartyAndRespondentHasSameLegalRepAndResponse() {
            // Given
            given(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(RESPONDENTSOLICITORTWO)))
                .willReturn(false);
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(wrapElements(UnavailableDate.builder()
                                                   .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                   .date(now().plusDays(5)).build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(hearing).build())
                .respondent2DQ(Respondent2DQ.builder().respondent2DQHearing(hearing).build())
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(YES)
                .build().toBuilder().ccdCaseReference(1234L)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldValidateExperts_whenMultipartyAndRespondentHasSameLegalRepAndResponseIsNull() {
            // Given
            given(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(RESPONDENTSOLICITORTWO)))
                .willReturn(false);
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(wrapElements(UnavailableDate.builder()
                                                   .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                   .date(now().plusDays(5)).build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(hearing).build())
                .respondent2DQ(Respondent2DQ.builder().respondent2DQHearing(hearing).build())
                .respondent2SameLegalRepresentative(YES)
                .build().toBuilder().ccdCaseReference(1234L)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldValidateExperts_whenMultipartyAndRespondentHasSameLegalRepDiffResponseRespondent2DQHearingIsNull() {
            // Given
            given(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(RESPONDENTSOLICITORTWO)))
                .willReturn(false);
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(wrapElements(UnavailableDate.builder()
                                                   .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                   .date(now().plusDays(5)).build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(hearing).build())
                .respondent2DQ(Respondent2DQ.builder().build())
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(NO)
                .build().toBuilder().ccdCaseReference(1234L)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldValidateExperts_whenMultipartyAndRespondentHasSameLegalRepDiffResponseRespondent2DQIsNull() {
            //Given
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(RESPONDENTSOLICITORTWO)))
                .thenReturn(false);
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(wrapElements(UnavailableDate.builder()
                                                   .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                   .date(now().plusDays(5)).build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(hearing).build())
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(NO)
                .build().toBuilder().ccdCaseReference(1234L)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldValidateExperts_whenDef2HasSameLRAndDiffResAndRespondent2DQHearingPresent() {
            //Given
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(RESPONDENTSOLICITORTWO)))
                .thenReturn(false);
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(wrapElements(UnavailableDate.builder()
                                                   .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                   .date(now().plusDays(5)).build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(hearing).build())
                .respondent2DQ(Respondent2DQ.builder().respondent2DQHearing(hearing).build())
                .build().toBuilder().ccdCaseReference(1234L)
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_whenUnavailableDateIsMoreThanOneYearInFuture() {
            //Given
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(wrapElements(UnavailableDate.builder()
                                                   .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                   .date(now().plusYears(5)).build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(hearing).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getErrors())
                .containsExactly("Dates must be within the next 12 months.");
        }

        @Test
        void shouldReturnError_whenUnavailableDateIsInPast() {
            //Given
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(wrapElements(UnavailableDate.builder()
                                                   .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                   .date(now().minusYears(5)).build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(hearing).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getErrors())
                .containsExactly("Unavailable Date cannot be past date");
        }

        @Test
        void shouldReturnNoError_whenUnavailableDateIsValid() {
            //Given
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(wrapElements(UnavailableDate.builder()
                                                   .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                   .date(now().plusDays(5)).build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(hearing).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenNoUnavailableDate() {
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(Hearing.builder().build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //Then
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
            //Given
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(RESPONDENTSOLICITORONE)))
                .thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent1DQ(Respondent1DQ
                                   .builder().respondent1DQExperts(Experts.builder()
                                                                       .expertRequired(NO)
                                                                       .build()).build())
                .build().toBuilder().ccdCaseReference(1234L)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldValidateExperts_whenMultipartyAndSolicitorRepresentsOnlyOneOfRespondentsAndResSolTwoRole() {
            //Given
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
                .thenReturn(false, true);
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent2DQ(Respondent2DQ
                                   .builder().respondent2DQExperts(Experts.builder().expertRequired(NO).build())
                                   .build())
                .build().toBuilder().ccdCaseReference(1234L)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldValidateExperts_whenMultipartyAndSameLRDiffResponseAndRespondent2DQExperts() {
            // Given
            given(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
                .willReturn(false, false);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .multiPartyClaimOneDefendantSolicitor()
                .respondent2DQ(Respondent2DQ
                                   .builder().respondent2DQExperts(Experts.builder().expertRequired(NO).build())
                                   .build())
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldValidateExperts_whenMultipartyAndDiffLRDiffResponse() {
            // Given
            given(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
                .willReturn(false, false);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .multiPartyClaimOneDefendantSolicitor()
                .respondent1DQ(Respondent1DQ
                                   .builder().respondent1DQExperts(Experts.builder().expertRequired(NO).build())
                                   .build())
                .respondent2SameLegalRepresentative(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldValidateExperts_whenMultipartyAndDiffLRResponseNullValidRespondent1DQExperts() {
            // Given
            given(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
                .willReturn(false, false);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .multiPartyClaimOneDefendantSolicitor()
                .respondent1DQ(Respondent1DQ
                                   .builder().respondent1DQExperts(Experts.builder().expertRequired(NO).build())
                                   .build())
                .respondent2SameLegalRepresentative(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldValidateExperts_whenMultipartyAndDiffLRResponseSameValidRespondent1DQExperts() {
            // Given
            given(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
                .willReturn(false, false);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .multiPartyClaimOneDefendantSolicitor()
                .respondent1DQ(Respondent1DQ
                                   .builder().respondent1DQExperts(Experts.builder().expertRequired(NO).build())
                                   .build())
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldValidateExperts_whenMultipartyAndDiffLRDiffResponseSameRespondent2DQNull() {
            // Given
            given(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
                .willReturn(false, false);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .multiPartyClaimOneDefendantSolicitor()
                .respondent1DQ(Respondent1DQ
                                   .builder().respondent1DQExperts(Experts.builder().expertRequired(NO).build())
                                   .build())
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldValidateExperts_whenMultipartyAndDiffLRDiffResponseSameRespondent2DQExpertsIsNull() {
            // Given
            given(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
                .willReturn(false, false);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .multiPartyClaimOneDefendantSolicitor()
                .respondent2DQ(Respondent2DQ
                                   .builder().build())
                .respondent1DQ(Respondent1DQ
                                   .builder().respondent1DQExperts(Experts.builder().expertRequired(NO).build())
                                   .build())
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_whenExpertRequiredAndNullDetails() {
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQExperts(Experts.builder()
                                                             .expertRequired(YES)
                                                             .build())
                                   .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("Expert details required");
        }

        @Test
        void shouldReturnNoError_whenExpertRequiredAndDetailsProvided() {
            //Given
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
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenExpertNotRequired() {
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQExperts(Experts.builder()
                                                             .expertRequired(NO)
                                                             .build())
                                   .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
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
            //Given
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(RESPONDENTSOLICITORONE)))
                .thenReturn(true);
            Witnesses witnesses = Witnesses.builder().witnessesToAppear(YES).build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQWitnesses(witnesses).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getErrors()).containsExactly("Witness details required");
        }

        @Test
        void shouldValidateWitness_whenMultipartyAndSolicitorRepresentsOnlyOneOfRespondentsAndResSolTwoRole() {
            //Given
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
                .thenReturn(false).thenReturn(true);
            Witnesses witnesses = Witnesses.builder().witnessesToAppear(YES).build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent2DQ(Respondent2DQ.builder().respondent2DQWitnesses(witnesses).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getErrors()).containsExactly("Witness details required");
        }

        @Test
        void shouldValidateWitness_whenMultipartyAndSameLRepAndRespondent1DQIsNull() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .multiPartyClaimOneDefendantSolicitor()
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When Respondent1DQ() is null and try to validate witness of Respondent1DQ()
            // Then throws Null pointer
            //Code Might need to validated to check Respondent1DQ() for Nulls
            assertThrows(
                NullPointerException.class,
                () -> handler.handle(params)
            );
        }

        @Test
        void shouldValidateWitness_whenMultipartyAndDiffLRepAndRespondent1DQIsNull() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .multiPartyClaimOneDefendantSolicitor()
                .respondent2SameLegalRepresentative(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When Respondent1DQ() is null and try to validate witness of Respondent1DQ()
            // Then throws Null pointer
            //Code Might need to validated to check Respondent1DQ() for Nulls
            assertThrows(
                NullPointerException.class,
                () -> handler.handle(params)
            );
        }

        @Test
        void shouldValidateWitness_whenMultipartyAndSameLRepRespondent2DQAndRespondent1DQIsNull() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .multiPartyClaimOneDefendantSolicitor()
                .respondent2DQ(null)
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When Respondent1DQ() is null and try to validate witness of Respondent1DQ()
            // Then throws Null pointer
            //Code Might need to validated to check Respondent1DQ() for Nulls
            assertThrows(
                NullPointerException.class,
                () -> handler.handle(params)
            );
        }

        @Test
        void shouldValidateWitness_whenMultipartyAndSameLRepRespondent2DQWitNullAndRespondent1DQIsNull() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .multiPartyClaimOneDefendantSolicitor()
                .respondent2DQ(Respondent2DQ.builder().respondent2DQWitnesses(null).build())
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When Respondent1DQ() is null and try to validate witness of Respondent1DQ()
            // Then throws Null pointer
            //Code Might need to validated to check Respondent1DQ() for Nulls
            assertThrows(
                NullPointerException.class,
                () -> handler.handle(params)
            );
        }

        @Test
        void shouldValidateWitness_whenMultipartyAndSameLRDiffResponseAndRespondent2DQWitness() {
            //Given
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
                .thenReturn(false);
            Witnesses witnesses = Witnesses.builder().witnessesToAppear(YES).build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .multiPartyClaimOneDefendantSolicitor()
                .respondent2DQ(Respondent2DQ.builder().respondent2DQWitnesses(witnesses).build())
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getErrors()).containsExactly("Witness details required");
        }

        @Test
        void shouldValidateWitness_whenMultipartyAndSameLRSameResponseAndRespondent1DQWitness() {
            // Given
            given(mockedStateFlow.isFlagSet(any())).willReturn(true, false);
            given(stateFlowEngine.evaluate(any(CaseData.class))).willReturn(mockedStateFlow);
            given(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class))).willReturn(false, false);
            Witnesses witnesses = Witnesses.builder().witnessesToAppear(YES).build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .multiPartyClaimOneDefendantSolicitor()
                .respondent2DQ(Respondent2DQ.builder().respondent2DQWitnesses(witnesses).build())
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(YES)
                .respondent1DQ(Respondent1DQ.builder().respondent1DQWitnesses(witnesses).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).containsExactly("Witness details required");
        }

        @Test
        void shouldReturnError_whenWitnessRequiredAndNullDetails() {
            //Given
            Witnesses witnesses = Witnesses.builder().witnessesToAppear(YES).build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQWitnesses(witnesses).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getErrors()).containsExactly("Witness details required");
        }

        @Test
        void shouldReturnNoError_whenWitnessRequiredAndDetailsProvided() {
            //Given
            List<Element<Witness>> testWitness = wrapElements(Witness.builder().build());
            Witnesses witnesses = Witnesses.builder().witnessesToAppear(YES).details(testWitness).build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQWitnesses(witnesses).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenWitnessNotRequired() {
            //Given
            Witnesses witnesses = Witnesses.builder().witnessesToAppear(NO).build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQWitnesses(witnesses).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidStatementOfTruth {

        @Test
        void shouldSetStatementOfTruthFieldsToNull_whenPopulated() {
            String name = "John Smith";
            String role = "Solicitor";
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .uiStatementOfTruth(StatementOfTruth.builder().name(name).role(role).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, "statement-of-truth");
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .extracting("uiStatementOfTruth")
                .doesNotHaveToString("name")
                .doesNotHaveToString("role");
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

            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        }

        @Test
        void shouldPopulateRespondent2Flag_WhenInvoked() {
            // Given
            given(featureToggleService.isCaseFileViewEnabled()).willReturn(true);
            given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(true);
            given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            // Then
            assertThat(response.getData()).containsEntry("respondent2DocumentGeneration", "userRespondent2");
        }

        @Test
        void shouldNotPopulateRespondent2Flag_WhenInvokedAndNoUser() {
            // Given
            given(featureToggleService.isCaseFileViewEnabled()).willReturn(true);
            given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
            given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            // Then
            assertThat(response.getData().get("respondent2DocumentGeneration")).isNull();
        }

        @Test
        void shouldNotPopulateRespondent2Flag_WhenInvoked() {
            // Given
            given(featureToggleService.isCaseFileViewEnabled()).willReturn(true);
            given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(true);
            given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            // Then
            assertThat(response.getData().get("respondent2DocumentGeneration")).isNull();
        }

        @Test
        void shouldAddPartyIdsToPartyFields_whenInvoked() {
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                .respondentResponseIsSame(YES)
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .build();

            when(featureToggleService.isHmcEnabled()).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("applicant1").hasFieldOrProperty("partyID");
            assertThat(response.getData()).extracting("respondent1").hasFieldOrProperty("partyID");
            assertThat(response.getData()).extracting("respondent2").hasFieldOrProperty("partyID");
        }

        @Test
        void shouldNotAddPartyIdsToPartyFields_whenInvokedWithHMCToggleOff() {
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                .respondentResponseIsSame(YES)
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .build();

            when(featureToggleService.isHmcEnabled()).thenReturn(false);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("applicant1")
                .isEqualTo(objectMapper.convertValue(caseData.getApplicant1(), HashMap.class));
            assertThat(response.getData()).extracting("respondent1")
                .isEqualTo(objectMapper.convertValue(caseData.getRespondent1(), HashMap.class));
            assertThat(response.getData()).extracting("respondent2")
                .isEqualTo(objectMapper.convertValue(caseData.getRespondent2(), HashMap.class));
        }

        @Test
        void shouldCopyRespondent1ResponseSetApplicantResponseAndSetBusinessProcess_whenOneRepGivingOneAnswer() {
            //Given
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                .respondentResponseIsSame(YES)
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
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
            //Given
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .respondentResponseIsSame(NO)
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("nextDeadline", deadline.toLocalDate().toString())
                .containsEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME))
                .containsEntry("respondent2ResponseDate", responseDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndSetBusinessProcess_when1v2SameSolicitorResponseIsNotTheSameInV1() {
            //Given
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .respondentResponseIsSame(NO)
                .respondent2SameLegalRepresentative(NO)
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("nextDeadline", deadline.toLocalDate().toString())
                .doesNotContainEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME))
                .containsEntry("respondent2ResponseDate", responseDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndSetBusinessProcess_whenSolicitorResponseRepNotTheSameWithNoCourtLocation() {
            //Given
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .respondentResponseIsSame(NO)
                .respondent2SameLegalRepresentative(NO)
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("nextDeadline", deadline.toLocalDate().toString())
                .doesNotContainEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME))
                .containsEntry("respondent2ResponseDate", responseDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndSetBusinessProcess_whenDiffLegalRepAndRespondent2NotPresent() {
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .addRespondent2(NO)
                .respondent2SameLegalRepresentative(NO)
                .respondent1Copy(PartyBuilder.builder()
                                     .individual()
                                     .legalRepHeading()
                                     .build())
                .respondent2Copy(PartyBuilder.builder()
                                     .individual()
                                     .legalRepHeading()
                                     .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("nextDeadline", deadline.toLocalDate().toString())
                .containsEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME))
                .doesNotContainEntry("respondent2ResponseDate", responseDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndSetBusinessProcess_whenDiffLegalRepAndRespondentIsNull() {
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .addRespondent2(null)
                .respondent2SameLegalRepresentative(NO)
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("nextDeadline", deadline.toLocalDate().toString())
                .containsEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME))
                .doesNotContainEntry("respondent2ResponseDate", responseDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndSetBusinessProcess_whenDiffLegalRepAndRespondent2Present() {
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent1Copy(PartyBuilder.builder()
                                     .individual()
                                     .legalRepHeading()
                                     .build())
                .respondent2Copy(PartyBuilder.builder()
                                     .individual()
                                     .legalRepHeading()
                                     .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("nextDeadline", deadline.toLocalDate().toString())
                .containsEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME))
                .doesNotContainEntry("respondent2ResponseDate", responseDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndSetBusinessProcess_when1v2SameSolicitorResponseAndClaimNotTheSame() {
            //Given
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .respondentResponseIsSame(NO)
                .respondent2ClaimResponseType(COUNTER_CLAIM)
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("nextDeadline", deadline.toLocalDate().toString())
                .containsEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME))
                .containsEntry("respondent2ResponseDate", responseDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndSetBusinessProcess_whenAtStateClaimSubmitted() {
            //Given
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atStateClaimSubmitted()
                .respondent1(PartyBuilder.builder().individual()
                                 .individualDateOfBirth(LocalDate.now().minusYears(1))
                                 .build())
                .respondent1ClaimResponseType(FULL_DEFENCE)
                .respondent2ClaimResponseType(FULL_DEFENCE)
                .respondent1DQ(
                    Respondent1DQ.builder().respondent1DQRequestedCourt(
                        RequestedCourt.builder()
                            .build()).build())
                .respondent2DQ(
                    Respondent2DQ.builder().respondent2DQRequestedCourt(
                        RequestedCourt.builder()
                            .build()).build())
                .respondentResponseIsSame(NO)
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("nextDeadline", deadline.toLocalDate().toString())
                .containsEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME))
                .containsEntry("respondent2ResponseDate", responseDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Test
        void shouldSetApplicantResponseDeadline_emptyPrimaryAddress() {
            //Given
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .respondentResponseIsSame(NO)
                .respondent1Copy(PartyBuilder.builder().individualNoPrimaryAddress("john").build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> handler.handle(params)
            );
            //Then
            assertEquals(exception.getMessage(), "Primary Address cannot be empty");
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndSetBusinessProcess_when1v2SameSolicitorResponseIsTheSame() {
            //Given
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

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("nextDeadline", deadline.toLocalDate().toString())
                .containsEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME))
                .containsEntry("respondent2ResponseDate", responseDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndSetBusinessProcess_when2ndRepAnsweringAfter1stAlreadyAnswered() {
            //Given
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                .respondent2Responds(FULL_DEFENCE)
                .respondent2DQ()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("nextDeadline", deadline.toLocalDate().toString())
                .containsEntry("respondent1ResponseDate", caseData.getRespondent1ResponseDate().format(ISO_DATE_TIME))
                .containsEntry("respondent2ResponseDate", responseDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndSetBusinessProcess_when1stRepAnsweringAfter2ndAlreadyAnswered() {
            //Given
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                .respondent2Responds(FULL_DEFENCE)
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("nextDeadline", deadline.toLocalDate().toString())
                .containsEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME))
                .containsEntry("respondent2ResponseDate", caseData.getRespondent2ResponseDate().format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Test
        void shouldSetDefendantResponseDocuments() {
            //Given
            when(time.now()).thenReturn(LocalDateTime.of(2022, 2, 18, 12, 10, 55));
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            @SuppressWarnings("unchecked")
            List<CaseDocument> docs = (ArrayList<CaseDocument>) response.getData().get("defendantResponseDocuments");
            //Then
            assertEquals(4, docs.size());
            assertThat(response.getData())
                .extracting("defendantResponseDocuments")
                .asString()
                .contains("createdBy=Defendant")
                .contains("documentName=defendant1-defence.pdf")
                .contains("documentSize=0")
                .contains("createdDatetime=2022-02-18T12:10:55")
                .contains(
                    "documentLink={document_url=http://dm-store:4506/documents/73526424-8434-4b1f-acca-bd33a3f8338f")
                .contains("documentType=DEFENDANT_DEFENCE")
                .contains("documentName=defendant2-defence.pdf")
                .contains("documentName=defendant1-directions.pdf")
                .contains("documentName=defendant2-directions.pdf")
                .contains("createdBy=Defendant 2")
                .contains("documentType=DEFENDANT_DRAFT_DIRECTIONS");
        }

        @Test
        void shouldAssignCategoryId_whenInvoked() {
            //Given
            when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
            when(time.now()).thenReturn(LocalDateTime.of(2022, 2, 18, 12, 10, 55));
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .extracting("defendantResponseDocuments")
                .asString()
                .contains("category_id=defendant1DefenseDirectionsQuestionnaire")
                .contains("category_id=defendant2DefenseDirectionsQuestionnaire");
        }

        @Test
        void shouldRetainSolicitorReferences_WhenNoReferencesPresent() {
            //Given
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .removeSolicitorReferences()
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT, Map.of());
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .doesNotHaveToString("solicitorReferences");
            assertThat(response.getData())
                .doesNotHaveToString("respondentSolicitor2Reference");
            assertThat(response.getData())
                .doesNotHaveToString("caseListDisplayDefendantSolicitorReferences");
        }

        @Test
        void shouldRetainSolicitorReferences_WhenAllReferencesPresent() {
            //Given
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build().toBuilder()
                .build();
            var beforeCaseData = Map.of("solicitorReferences",
                                        Map.of("applicantSolicitor1Reference", "12345",
                                               "respondentSolicitor1Reference", "6789"
                                        ),
                                        "respondentSolicitor2Reference", "01234"
            );

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT, beforeCaseData);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .extracting("solicitorReferences")
                .asString()
                .contains("applicantSolicitor1Reference=12345")
                .contains("respondentSolicitor1Reference=6789")
                .doesNotContain("respondentSolicitor2Reference");
            assertThat(response.getData())
                .extracting("respondentSolicitor2Reference")
                .asString()
                .contains("01234");
            assertThat(response.getData())
                .extracting("caseListDisplayDefendantSolicitorReferences")
                .asString()
                .contains("6789, 01234");
        }

        @Test
        void shouldNotSetApplicantResponseDeadlineOrTransitionCcdState_when1stRespondentAnsweringBefore2nd() {
            //Given
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting2ndRespondentResponse()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).doesNotHaveToString("applicant1ResponseDeadline");
            assertThat(response.getData())
                .containsEntry("nextDeadline", caseData.getRespondent2ResponseDeadline().toLocalDate().toString());
            assertThat(response.getData()).extracting("respondent1ResponseDate")
                .isEqualTo(responseDate.format(ISO_DATE_TIME));
            assertThat(response.getData()).doesNotHaveToString("respondent2ResponseDate");
            assertThat(response.getState()).isNull();
        }

        @Test
        void shouldNotSetApplicantResponseDeadlineOrTransitionCcdState_when2ndRespondentAnsweringBefore1st() {
            //Given
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting1stRespondentResponse()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).doesNotHaveToString("applicant1ResponseDeadline");
            assertThat(response.getData())
                .containsEntry("nextDeadline", caseData.getRespondent1ResponseDeadline().toLocalDate().toString());
            assertThat(response.getData()).extracting("respondent2ResponseDate")
                .isEqualTo(responseDate.format(ISO_DATE_TIME));
            assertThat(response.getData()).doesNotHaveToString("respondent1ResponseDate");
            assertThat(response.getState()).isNull();
        }

        @Test
        void shouldCopyRespondentPrimaryAddresses_whenInvoked() {
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                .respondent2ResponseDeadline(RESPONSE_DEADLINE)
                .respondent2(PartyBuilder.builder().individual().build())
                .build();
            String address = "test address";
            var expectedAddress = AddressBuilder.defaults().addressLine1(address).build();
            caseData = caseData.toBuilder()
                .respondent1Copy(caseData.getRespondent1().toBuilder().primaryAddress(expectedAddress).build())
                .respondent2Copy(caseData.getRespondent2().toBuilder().primaryAddress(expectedAddress).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).doesNotContainKey("respondent1Copy");
            assertThat(response.getData())
                .extracting("respondent1").extracting("primaryAddress").extracting("AddressLine1").isEqualTo(address);
            assertThat(response.getData())
                .extracting("respondent2").extracting("primaryAddress").extracting("AddressLine1").isEqualTo(address);
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndSetBusinessProcess_whenOneDefendantRepAnsweringToTwoApplicants() {
            //Given
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoApplicants()
                .atStateClaimDetailsNotified()
                .respondent1ClaimResponseTypeToApplicant1(FULL_DEFENCE)
                .respondent1ClaimResponseTypeToApplicant2(FULL_DEFENCE)
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME));

            assertThat(response.getData()).doesNotHaveToString("respondent2ResponseDate");
            assertThat(response.getData())
                .containsEntry("nextDeadline", deadline.toLocalDate().toString());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndSetBusinessProcess_whenOneDefendantRepAnsweringToTwoApplicantsInV1() {
            //Given
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoApplicants()
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(YES)
                .atStateClaimDetailsNotified()
                .respondent1ClaimResponseTypeToApplicant1(FULL_DEFENCE)
                .respondent1ClaimResponseTypeToApplicant2(FULL_DEFENCE)
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME));

            assertThat(response.getData()).doesNotHaveToString("respondent2ResponseDate");
            assertThat(response.getData())
                .containsEntry("nextDeadline", deadline.toLocalDate().toString());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndSetBusinessProcess_when1DefendantRepAnswerTo2ApplicantInV1DiffResponse() {
            //Given
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(wrapElements(UnavailableDate.builder()
                                                   .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                   .date(now().plusDays(5)).build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoApplicants()
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(NO)
                .atStateClaimDetailsNotified()
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent1ClaimResponseTypeToApplicant1(FULL_DEFENCE)
                .respondent1ClaimResponseTypeToApplicant2(FULL_DEFENCE)
                .respondent2ClaimResponseType(FULL_DEFENCE)
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .respondent2DQ(Respondent2DQ.builder().respondent2DQHearing(hearing).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME));

            assertThat(response.getData()).doesNotHaveToString("respondent2ResponseDate");
            assertThat(response.getData())
                .containsEntry("nextDeadline", deadline.toLocalDate().toString());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndSetBusinessProcess_when1DefendantRepAnswerTo2ApplicantInV1DiffClaim() {
            //Given
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);

            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(wrapElements(UnavailableDate.builder()
                                                   .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                   .date(now().plusDays(5)).build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoApplicants()
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(NO)
                .atStateClaimDetailsNotified()
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent1ClaimResponseTypeToApplicant1(FULL_DEFENCE)
                .respondent1ClaimResponseTypeToApplicant2(FULL_DEFENCE)
                .respondent1ClaimResponseType(COUNTER_CLAIM)
                .respondent2ClaimResponseType(FULL_DEFENCE)
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .respondent2DQ(Respondent2DQ.builder().respondent2DQHearing(hearing).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .containsEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .containsEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME));

            assertThat(response.getData()).doesNotHaveToString("respondent2ResponseDate");
            assertThat(response.getData())
                .containsEntry("nextDeadline", deadline.toLocalDate().toString());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(DEFENDANT_RESPONSE.name(), "READY");
        }

        @Test
        void shouldNotSetApplicantResponseDeadlineAndSetBusinessProcess_whenRespondentResponseIsNotSet() {
            //Given
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoApplicants()
                .respondent2SameLegalRepresentative(YES)
                .atStateClaimDetailsNotified()
                .respondent2ClaimResponseType(COUNTER_CLAIM)
                .respondent1ClaimResponseTypeToApplicant1(FULL_DEFENCE)
                .respondent1ClaimResponseTypeToApplicant2(FULL_DEFENCE)
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .doesNotContainEntry("applicant1ResponseDeadline", deadline.format(ISO_DATE_TIME))
                .doesNotContainEntry("respondent1ResponseDate", responseDate.format(ISO_DATE_TIME));
            assertThat(response.getData())
                .doesNotContainEntry("nextDeadline", deadline.toLocalDate().toString());
            assertThat(response.getData()).doesNotHaveToString("respondent2ResponseDate");
        }

        @Test
        void shouldSetApplicantResponseDeadlineAndSetBusinessProcess_whenRespondent2CopyIsNull() {
            //Given
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoApplicants()
                .respondentResponseIsSame(YES)
                .atStateClaimDetailsNotified()
                .respondent1ClaimResponseType(FULL_DEFENCE)
                .respondent1ClaimResponseTypeToApplicant1(FULL_DEFENCE)
                .respondent1ClaimResponseTypeToApplicant2(FULL_DEFENCE)
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            //Then
            assertThrows(
                NullPointerException.class,
                () -> handler.handle(params)
            );
        }

        @Nested
        class ResetStatementOfTruth {

            @Test
            void shouldMoveStatementOfTruthToCorrectFieldAndResetUIField_when1V1() {
                String name = "John Smith";
                String role = "Solicitor";
                //Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                    .respondent1Copy(PartyBuilder.builder().individual().build())
                    .uiStatementOfTruth(StatementOfTruth.builder().name(name).role(role).build())
                    .build();
                CallbackParams callbackParams = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                //When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
                //Then
                assertThat(response.getData())
                    .extracting("respondent1DQStatementOfTruth")
                    .extracting("name", "role")
                    .containsExactly(name, role);

                assertThat(response.getData())
                    .extracting("uiStatementOfTruth")
                    .doesNotHaveToString("name")
                    .doesNotHaveToString("role");
            }

            @Test
            void shouldMoveStatementOfTruthToCorrectFieldAndResetUIField_when2V1SameRep() {
                String name = "John Smith";
                String role = "Solicitor";
                //Given
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoApplicants()
                    .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                    .build()
                    .toBuilder()
                    .respondent1Copy(PartyBuilder.builder().individual().build())
                    .uiStatementOfTruth(StatementOfTruth.builder().name(name).role(role).build())
                    .build();
                CallbackParams callbackParams = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                //When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
                //Then
                assertThat(response.getData())
                    .extracting("respondent1DQStatementOfTruth")
                    .extracting("name", "role")
                    .containsExactly(name, role);

                assertThat(response.getData())
                    .extracting("uiStatementOfTruth")
                    .doesNotHaveToString("name")
                    .doesNotHaveToString("role");
            }
        }

        @Nested
        class HandleCourtLocation {

            @Nested
            class OneVOne {
                @Test
                void shouldHandleCourtLocationData_1v1() {
                    //Given
                    LocationRefData locationA = LocationRefData.builder()
                        .regionId("regionId1").epimmsId("epimmsId1").courtLocationCode("312").siteName("Site 1")
                        .courtAddress("Lane 1").postcode("123").build();
                    when(courtLocationUtils.findPreferredLocationData(any(), any(DynamicList.class)))
                        .thenReturn(locationA);

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                        .respondent1Copy(PartyBuilder.builder().individual().build())
                        .respondent1DQ(
                            Respondent1DQ.builder().respondent1DQRequestedCourt(
                                RequestedCourt.builder()
                                    .responseCourtLocations(DynamicList.fromList(
                                        Collections.singletonList(locationA),
                                        LocationRefDataService::getDisplayEntry,
                                        locationA,
                                        false
                                    ))
                                    .build()).build())
                        .build();

                    CallbackParams callbackParams = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                    //When
                    var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
                    //Then
                    assertThat(response.getData())
                        .extracting("respondent1DQRequestedCourt")
                        .extracting("responseCourtLocations").isNull();

                    assertThat(response.getData())
                        .extracting("respondent1DQRequestedCourt")
                        .extracting("caseLocation")
                        .extracting("region", "baseLocation")
                        .containsExactly("regionId1", "epimmsId1");

                    assertThat(response.getData())
                        .extracting("respondent1DQRequestedCourt")
                        .extracting("responseCourtCode").isEqualTo("312");
                }
            }

            @Nested
            class OneVTwoSameSolicitor {
                @Test
                void shouldHandleCourtLocationData_SameResponse() {
                    //Given
                    when(coreCaseUserService.userHasCaseRole(any(), any(),
                                                             eq(RESPONDENTSOLICITORONE)
                    )).thenReturn(true);
                    when(coreCaseUserService.userHasCaseRole(any(), any(),
                                                             eq(RESPONDENTSOLICITORTWO)
                    )).thenReturn(true);

                    LocationRefData locationA = LocationRefData.builder()
                        .regionId("regionId1").epimmsId("epimmsId1").courtLocationCode("312").siteName("Site 1")
                        .courtAddress("Lane 1").postcode("123").build();
                    when(courtLocationUtils.findPreferredLocationData(any(), any(DynamicList.class)))
                        .thenReturn(locationA);

                    CaseData caseData = CaseDataBuilder.builder()
                        .multiPartyClaimOneDefendantSolicitor()
                        .atStateRespondentFullDefence()
                        .respondentResponseIsSame(YES)
                        .respondent1Copy(PartyBuilder.builder().individual().build())
                        .respondent2Copy(PartyBuilder.builder().individual().build())
                        .respondent1DQ(
                            Respondent1DQ.builder().respondent1DQRequestedCourt(
                                RequestedCourt.builder()
                                    .responseCourtLocations(DynamicList.fromList(
                                        Collections.singletonList(locationA),
                                        LocationRefDataService::getDisplayEntry,
                                        locationA,
                                        false
                                    ))
                                    .build()).build())
                        .build();

                    CallbackParams callbackParams = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                    //When
                    var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
                    //Then
                    assertThat(response.getData())
                        .extracting("respondent1DQRequestedCourt")
                        .extracting("responseCourtLocations").isNull();

                    assertThat(response.getData())
                        .extracting("respondent1DQRequestedCourt")
                        .extracting("caseLocation")
                        .extracting("region", "baseLocation")
                        .containsExactly("regionId1", "epimmsId1");

                    assertThat(response.getData())
                        .extracting("respondent1DQRequestedCourt")
                        .extracting("responseCourtCode").isEqualTo("312");
                }

                @Test
                void shouldHandleCourtLocationData_DifferentResponse() {
                    //Given
                    when(coreCaseUserService.userHasCaseRole(any(), any(),
                                                             eq(RESPONDENTSOLICITORONE)
                    )).thenReturn(true);
                    when(coreCaseUserService.userHasCaseRole(any(), any(),
                                                             eq(RESPONDENTSOLICITORTWO)
                    )).thenReturn(true);

                    LocationRefData locationA = LocationRefData.builder()
                        .regionId("regionId1").epimmsId("epimmsId1").courtLocationCode("312").siteName("Site 1")
                        .courtAddress("Lane 1").postcode("123").build();
                    when(courtLocationUtils.findPreferredLocationData(any(), any(DynamicList.class)))
                        .thenReturn(locationA);
                    CaseData caseData = CaseDataBuilder.builder()
                        .multiPartyClaimOneDefendantSolicitor()
                        .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                        .respondentResponseIsSame(NO)
                        .respondent1Copy(PartyBuilder.builder().individual().build())
                        .respondent2Copy(PartyBuilder.builder().individual().build())
                        .respondent1DQ(
                            Respondent1DQ.builder().respondent1DQRequestedCourt(
                                RequestedCourt.builder()
                                    .responseCourtLocations(
                                        DynamicList.fromList(
                                            Collections.singletonList(locationA),
                                            LocationRefDataService::getDisplayEntry,
                                            locationA,
                                            false
                                        ))
                                    .build()).build())
                        .respondent2DQ(
                            Respondent2DQ.builder().respondent2DQRequestedCourt(
                                RequestedCourt.builder()
                                    .responseCourtLocations(DynamicList.fromList(
                                        Collections.singletonList(locationA),
                                        LocationRefDataService::getDisplayEntry,
                                        locationA,
                                        false
                                    ))
                                    .build()).build())
                        .build();

                    CallbackParams callbackParams = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                    //When
                    var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
                    //Then
                    assertThat(response.getData())
                        .extracting("respondent1DQRequestedCourt")
                        .extracting("responseCourtLocations").isNull();

                    assertThat(response.getData())
                        .extracting("respondent1DQRequestedCourt")
                        .extracting("caseLocation")
                        .extracting("region", "baseLocation")
                        .containsExactly("regionId1", "epimmsId1");

                    assertThat(response.getData())
                        .extracting("respondent1DQRequestedCourt")
                        .extracting("responseCourtCode").isEqualTo("312");

                    assertThat(response.getData())
                        .extracting("respondent2DQRequestedCourt")
                        .extracting("responseCourtLocations").isNull();

                    assertThat(response.getData())
                        .extracting("respondent2DQRequestedCourt")
                        .extracting("caseLocation")
                        .extracting("region", "baseLocation")
                        .containsExactly("regionId1", "epimmsId1");

                    assertThat(response.getData())
                        .extracting("respondent2DQRequestedCourt")
                        .extracting("responseCourtCode").isEqualTo("312");
                }
            }

            @Nested
            class OneVTwoDifferentSolicitor {
                @Test
                void shouldHandleCourtLocationData_when2ndRespondentAnsweringBefore1st() {
                    //Given
                    when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO)))
                        .thenReturn(true);

                    LocationRefData locationA = LocationRefData.builder()
                        .regionId("regionId1").epimmsId("epimmsId1").courtLocationCode("312").siteName("Site 1")
                        .courtAddress("Lane 1").postcode("123").build();
                    when(courtLocationUtils.findPreferredLocationData(any(), any(DynamicList.class)))
                        .thenReturn(locationA);

                    CaseData caseData = CaseDataBuilder.builder()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                        .respondent2Responds(FULL_DEFENCE)
                        .respondent2DQ(
                            Respondent2DQ.builder().respondent2DQRequestedCourt(
                                RequestedCourt.builder()
                                    .responseCourtLocations(
                                        DynamicList.fromList(
                                            Collections.singletonList(locationA),
                                            LocationRefDataService::getDisplayEntry,
                                            locationA,
                                            false
                                        )
                                    )
                                    .build()).build())
                        .respondent1Copy(PartyBuilder.builder().individual().build())
                        .respondent2Copy(PartyBuilder.builder().individual().build())
                        .build();

                    CallbackParams callbackParams = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                    //When
                    var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
                    //Then
                    assertThat(response.getData())
                        .extracting("respondent2DQRequestedCourt")
                        .extracting("responseCourtLocations").isNull();

                    assertThat(response.getData())
                        .extracting("respondent2DQRequestedCourt")
                        .extracting("caseLocation")
                        .extracting("region", "baseLocation")
                        .containsExactly("regionId1", "epimmsId1");

                    assertThat(response.getData())
                        .extracting("respondent2DQRequestedCourt")
                        .extracting("responseCourtCode").isEqualTo("312");
                }

                @Test
                void shouldHandleCourtLocationData_when2ndRespondentWithNoDynamicValForCourtLocation() {
                    //Given
                    when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO)))
                        .thenReturn(true);
                    CaseData caseData = CaseDataBuilder.builder()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                        .respondent2Responds(FULL_DEFENCE)
                        .respondent2DQ(
                            Respondent2DQ.builder().respondent2DQRequestedCourt(
                                RequestedCourt.builder()
                                    .responseCourtLocations(DynamicList.builder().build())
                                    .build()).build())
                        .respondent1Copy(PartyBuilder.builder().individual().build())
                        .respondent2Copy(PartyBuilder.builder().individual().build())
                        .build();

                    CallbackParams callbackParams = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                    //When No dynamic values provided
                    var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                    //Then
                    assertThat(response.getData())
                        .extracting("respondent2DQRequestedCourt")
                        .extracting("responseCourtLocations").isNull();

                }

                @Test
                void shouldHandleCourtLocationData_when2ndRespondentResponseCourtLocationIsNotPresent() {
                    //Given
                    when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO)))
                        .thenReturn(true);
                    CaseData caseData = CaseDataBuilder.builder()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                        .respondent2Responds(FULL_DEFENCE)
                        .respondent2DQ(
                            Respondent2DQ.builder().respondent2DQRequestedCourt(
                                RequestedCourt.builder()
                                    .build()).build())
                        .respondent1Copy(PartyBuilder.builder().individual().build())
                        .respondent2Copy(PartyBuilder.builder().individual().build())
                        .build();

                    CallbackParams callbackParams = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                    //When No Response court locations is present
                    var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                    //Then
                    assertThat(response.getData())
                        .extracting("respondent2DQRequestedCourt")
                        .extracting("responseCourtLocations").isNull();

                }

                @Test
                void shouldHandleCourtLocationData_when1stRespondentAnsweringBefore2nd() {
                    //Given
                    when(coreCaseUserService.userHasCaseRole(any(), any(),
                                                             eq(RESPONDENTSOLICITORONE)
                    )).thenReturn(true);

                    LocationRefData locationA = LocationRefData.builder()
                        .regionId("regionId1").epimmsId("epimmsId1").courtLocationCode("312").siteName("Site 1")
                        .courtAddress("Lane 1").postcode("123").build();
                    when(courtLocationUtils.findPreferredLocationData(any(), any(DynamicList.class)))
                        .thenReturn(locationA);
                    CaseData caseData = CaseDataBuilder.builder()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting2ndRespondentResponse()
                        .respondent1Copy(PartyBuilder.builder().individual().build())
                        .respondent2Copy(PartyBuilder.builder().individual().build())
                        .respondent1DQ(
                            Respondent1DQ.builder().respondent1DQRequestedCourt(
                                RequestedCourt.builder()
                                    .responseCourtLocations(DynamicList.fromList(
                                        Collections.singletonList(locationA),
                                        LocationRefDataService::getDisplayEntry,
                                        locationA,
                                        false
                                    ))
                                    .responseCourtCode("312")
                                    .build()).build())
                        .build();

                    CallbackParams callbackParams = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                    //When
                    var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
                    //Then
                    assertThat(response.getData())
                        .extracting("respondent1DQRequestedCourt")
                        .extracting("responseCourtLocations").isNull();

                    assertThat(response.getData())
                        .extracting("respondent1DQRequestedCourt")
                        .extracting("caseLocation")
                        .extracting("region", "baseLocation")
                        .containsExactly("regionId1", "epimmsId1");

                    assertThat(response.getData())
                        .extracting("respondent1DQRequestedCourt")
                        .extracting("responseCourtCode").isEqualTo("312");
                }

                @Test
                void shouldHandleCourtLocationData_when1stRespondentWithNoDynamicValForCourtLocation() {
                    //Given
                    when(coreCaseUserService.userHasCaseRole(any(), any(),
                                                             eq(RESPONDENTSOLICITORONE)
                    )).thenReturn(true);

                    CaseData caseData = CaseDataBuilder.builder()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting2ndRespondentResponse()
                        .respondent1Copy(PartyBuilder.builder().individual().build())
                        .respondent2Copy(PartyBuilder.builder().individual().build())
                        .respondent1DQ(
                            Respondent1DQ.builder().respondent1DQRequestedCourt(
                                RequestedCourt.builder()
                                    .responseCourtLocations(DynamicList.builder().build())
                                    .responseCourtCode("312")
                                    .build()).build())
                        .build();

                    CallbackParams callbackParams = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                    //When
                    var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
                    //Then
                    assertThat(response.getData())
                        .extracting("respondent1DQRequestedCourt")
                        .extracting("responseCourtLocations").isNull();
                }
            }

            @Nested
            class NoCourtChosen {
                @Test
                void shouldHandleCourtLocationData_1v1() {
                    //Given
                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                        .respondent1Copy(PartyBuilder.builder().individual().build())
                        .respondent1DQ(
                            Respondent1DQ.builder().respondent1DQRequestedCourt(
                                RequestedCourt.builder()
                                    .build()).build())
                        .build();

                    CallbackParams callbackParams = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                    //When
                    var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
                    //Then
                    assertThat(response.getData())
                        .extracting("respondent1DQRequestedCourt")
                        .extracting("responseCourtLocations").isNull();

                    assertThat(response.getData())
                        .extracting("respondent1DQRequestedCourt")
                        .extracting("caseLocation")
                        .isNull();

                    assertThat(response.getData())
                        .extracting("respondent1DQRequestedCourt")
                        .extracting("responseCourtCode").isNull();
                }

                @Test
                void shouldHandleCourtLocationData_when1stRespondentAnsweringBefore2nd() {
                    //Given
                    CaseData caseData = CaseDataBuilder.builder()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting2ndRespondentResponse()
                        .respondent1Copy(PartyBuilder.builder().individual().build())
                        .respondent2Copy(PartyBuilder.builder().individual().build())
                        .respondent1DQ(
                            Respondent1DQ.builder().respondent1DQRequestedCourt(
                                RequestedCourt.builder()
                                    .build()).build())
                        .build();

                    CallbackParams callbackParams = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                    //When
                    var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
                    //Then
                    assertThat(response.getData())
                        .extracting("respondent1DQRequestedCourt")
                        .extracting("responseCourtLocations").isNull();

                    assertThat(response.getData())
                        .extracting("respondent1DQRequestedCourt")
                        .extracting("caseLocation")
                        .isNull();

                    assertThat(response.getData())
                        .extracting("respondent1DQRequestedCourt")
                        .extracting("responseCourtCode").isNull();
                }
            }
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnConfirmationScreen_when1v1ResponseSubmitted() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            //When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            //Then
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

        @Test
        void shouldReturnConfirmationScreen_when1v2_andReceivedFirstResponse() {
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            //When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            //Then
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
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            //When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            //Then
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

        @BeforeEach
        void setup() {
            when(mockedStateFlow.isFlagSet(any())).thenReturn(false);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        }

        private static final String PAGE_ID = "set-generic-response-type-flag";

        @Test
        void shouldSetMultiPartyResponseTypeFlags_Respondent1IsFullDefence() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags").isEqualTo("FULL_DEFENCE");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_when1v2DifferentSolicitorsAndRespondent2IsFullDefence() {
            //Given
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(CaseRole.RESPONDENTSOLICITORTWO)))
                .thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting1stRespondentResponse()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags").isEqualTo("FULL_DEFENCE");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_when1v2SameSolicitorsAndRespondent2IsFullDefence() {
            //Given
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(CaseRole.RESPONDENTSOLICITORTWO)))
                .thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence_1v2_Resp1CounterClaimAndResp2FullDefence()
                .multiPartyClaimOneDefendantSolicitor()
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags").isEqualTo("FULL_DEFENCE");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_2v1Only1FullDefence() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().multiPartyClaimTwoApplicants().build().toBuilder()
                .respondent1ClaimResponseType(COUNTER_CLAIM)
                .respondent1ClaimResponseTypeToApplicant2(FULL_DEFENCE)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags").isEqualTo("FULL_DEFENCE");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_2v1BothFullDefence() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().multiPartyClaimTwoApplicants().build().toBuilder()
                .respondent1ClaimResponseType(FULL_DEFENCE)
                .respondent1ClaimResponseTypeToApplicant2(FULL_DEFENCE)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags").isEqualTo("FULL_DEFENCE");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_2v1PartAdmission() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().multiPartyClaimTwoApplicants().build().toBuilder()
                .respondent1ClaimResponseType(COUNTER_CLAIM)
                .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags").isEqualTo("PART_ADMISSION");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_2v1PartAdmissionv2FullDefence() {
            //Given
            when(mockedStateFlow.isFlagSet(any())).thenReturn(false);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), any())).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().multiPartyClaimTwoApplicants().build().toBuilder()
                .respondent1ClaimResponseType(COUNTER_CLAIM)
                .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                .respondent2ClaimResponseType(FULL_DEFENCE)
                .respondent2SameLegalRepresentative(YES)
                .build().toBuilder().ccdCaseReference(1234L)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags").isEqualTo("FULL_DEFENCE");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_2v1PartAdmissionSolicitorAndR1ResponseMatch() {
            //Given
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), any())).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().multiPartyClaimTwoApplicants().build().toBuilder()
                .respondent1ClaimResponseType(FULL_DEFENCE)
                .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                .respondent2ClaimResponseType(COUNTER_CLAIM)
                .respondent2SameLegalRepresentative(YES)
                .build().toBuilder().ccdCaseReference(1234L)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags").isEqualTo("FULL_DEFENCE");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_2v1PartAdmissionSolicitorAndR2ResponseMatch() {
            //Given
            when(mockedStateFlow.isFlagSet(any())).thenReturn(false);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), any())).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().multiPartyClaimTwoApplicants().build().toBuilder()
                .respondent1ClaimResponseType(COUNTER_CLAIM)
                .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                .respondent2ClaimResponseType(FULL_DEFENCE)
                .respondent2SameLegalRepresentative(YES)
                .build().toBuilder().ccdCaseReference(1234L)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags").isEqualTo("FULL_DEFENCE");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_2v1PartAdmissionSolicitorResponseNotMatch() {
            //Given
            when(mockedStateFlow.isFlagSet(any())).thenReturn(false);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), any())).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().multiPartyClaimTwoApplicants().build().toBuilder()
                .respondent1ClaimResponseType(COUNTER_CLAIM)
                .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                .respondent2ClaimResponseType(COUNTER_CLAIM)
                .respondent2SameLegalRepresentative(YES)
                .build().toBuilder().ccdCaseReference(1234L)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags").isEqualTo("PART_ADMISSION");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_2v1PartAdmissionFullDefenceForBothDefendants() {
            //Given
            when(mockedStateFlow.isFlagSet(any())).thenReturn(false);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), any())).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().multiPartyClaimTwoApplicants().build().toBuilder()
                .respondent1ClaimResponseType(FULL_DEFENCE)
                .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                .respondent2ClaimResponseType(FULL_DEFENCE)
                .respondent2SameLegalRepresentative(YES)
                .build().toBuilder().ccdCaseReference(1234L)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags").isEqualTo("FULL_DEFENCE");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_WhenFullDefenceForBothDefendantsRes2DiffLegalRep() {
            //Given
            when(mockedStateFlow.isFlagSet(any())).thenReturn(false);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), any())).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().multiPartyClaimTwoApplicants().build().toBuilder()
                .respondent1ClaimResponseType(FULL_DEFENCE)
                .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                .respondent2ClaimResponseType(FULL_DEFENCE)
                .respondent2SameLegalRepresentative(NO)
                .build().toBuilder().ccdCaseReference(1234L)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags").isEqualTo("FULL_DEFENCE");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_2v1PartAdmissionRespondent1ResponseIsMatching() {
            //Given
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), any())).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().multiPartyClaimTwoApplicants().build().toBuilder()
                .respondent1ClaimResponseType(FULL_DEFENCE)
                .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                .respondent2ClaimResponseType(COUNTER_CLAIM)
                .respondent2SameLegalRepresentative(NO)
                .build().toBuilder().ccdCaseReference(1234L)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags").isEqualTo("FULL_DEFENCE");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_2v1PartAdmissionRespondent1ResponseNotMatching() {
            //Given
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), any())).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().multiPartyClaimTwoApplicants().build().toBuilder()
                .respondent1ClaimResponseType(FULL_DEFENCE)
                .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                .respondent2ClaimResponseType(COUNTER_CLAIM)
                .respondent2SameLegalRepresentative(NO)
                .addApplicant2(NO)
                .build().toBuilder().ccdCaseReference(1234L)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags").isEqualTo("NOT_FULL_DEFENCE");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_2v1PartAdmissionRespondent1ResponseNotMatchingAddApp2() {
            //Given
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), any())).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().multiPartyClaimTwoApplicants().build().toBuilder()
                .respondent1ClaimResponseType(FULL_DEFENCE)
                .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                .respondent2ClaimResponseType(COUNTER_CLAIM)
                .respondent2SameLegalRepresentative(NO)
                .addApplicant2(YES)
                .build().toBuilder().ccdCaseReference(1234L)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags").isEqualTo("FULL_DEFENCE");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_2v1NonFullDefence() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().multiPartyClaimTwoApplicants().build().toBuilder()
                .respondent1ClaimResponseType(COUNTER_CLAIM)
                .respondent1ClaimResponseTypeToApplicant2(COUNTER_CLAIM)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags").isEqualTo("NOT_FULL_DEFENCE");
        }

        @Test
        void shouldReturnError_WhenBothFullDefenceAndSameRespondent() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
                .respondent2ClaimResponseType(FULL_DEFENCE)
                .respondent2SameLegalRepresentative(YES)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getErrors())
                .containsExactly("It is not possible to respond for both defendants with Reject all of the claim. "
                                     + "Please go back and select single response option.");
        }

        @Test
        void shouldNotReturnError_WhenNotBothFullDefence() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
                .respondent2ClaimResponseType(PART_ADMISSION)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getErrors())
                .isEmpty();
        }
    }
}
