package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUnavailabilityDates;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralAppSampleDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService;
import uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationServiceHelper;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.LocalDate.EPOCH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration.OTHER;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingSupportRequirements.OTHER_SUPPORT;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingType.IN_PERSON;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.INVALID_TRIAL_DATE_RANGE;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.INVALID_UNAVAILABILITY_RANGE;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.TRIAL_DATE_FROM_REQUIRED;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.UNAVAILABLE_DATE_RANGE_MISSING;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.UNAVAILABLE_FROM_MUST_BE_PROVIDED;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.URGENCY_DATE_CANNOT_BE_IN_PAST;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.URGENCY_DATE_REQUIRED;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.URGENCY_DATE_SHOULD_NOT_BE_PROVIDED;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    InitiateGeneralApplicationHandler.class,
    JacksonAutoConfiguration.class,
})
class InitiateGeneralApplicationHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private InitiateGeneralApplicationHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InitiateGeneralApplicationService initiateGeneralAppService;

    @MockBean
    private InitiateGeneralApplicationServiceHelper helper;

    @MockBean
    private OrganisationService organisationService;

    private static final String STRING_CONSTANT = "this is a string";
    private static final DynamicList PBA_ACCOUNTS = DynamicList.builder().build();
    private static final LocalDate APP_DATE_EPOCH = EPOCH;

    @Nested
    class AboutToStartCallback extends GeneralAppSampleDataBuilder {

        private final Organisation organisation = Organisation.builder()
                .paymentAccount(List.of("12345", "98765"))
                .build();

        @Test
        void shouldCalculateClaimFeeAndAddPbaNumbers_whenCalledAndOrgExistsInPrd() {
            given(organisationService.findOrganisation(any())).willReturn(Optional.of(organisation));
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            DynamicList dynamicList = getDynamicList(response);
            List<String> actualPbas = dynamicList.getListItems().stream()
                    .map(DynamicListElement::getLabel)
                    .collect(Collectors.toList());

            assertThat(actualPbas).containsOnly("12345", "98765");
            assertThat(dynamicList.getValue()).isEqualTo(DynamicListElement.EMPTY);
        }

        @Test
        void shouldCalculateClaimFee_whenCalledAndOrgDoesNotExistInPrd() {
            given(organisationService.findOrganisation(any())).willReturn(Optional.empty());

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(getDynamicList(response))
                    .isEqualTo(DynamicList.builder()
                            .value(DynamicListElement.builder().code(null).label(null).build())
                            .listItems(Collections.<DynamicListElement>emptyList()).build());
        }

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }

        private DynamicList getDynamicList(AboutToStartOrSubmitCallbackResponse response) {
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            return responseCaseData.getGeneralAppPBADetails().getApplicantsPbaAccounts();
        }
    }

    @Nested
    class MidEventForUrgencyCheck extends GeneralAppSampleDataBuilder {

        private static final String VALIDATE_URGENCY_DATE_PAGE = "ga-validate-urgency-date";

        @Test
        void shouldNotCauseAnyErrors_whenApplicationDetailsNotProvided() {
            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);
            when(initiateGeneralAppService.validateUrgencyDates(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnErrors_whenApplicationIsUrgentButConsiderationDateIsNotProvided() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
                                                        true, null);

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);
            when(initiateGeneralAppService.validateUrgencyDates(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(URGENCY_DATE_REQUIRED);
        }

        @Test
        void shouldReturnErrors_whenApplicationIsNotUrgentButConsiderationDateIsProvided() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
                                                        false, LocalDate.now());

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);
            when(initiateGeneralAppService.validateUrgencyDates(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(
                    URGENCY_DATE_SHOULD_NOT_BE_PROVIDED);
        }

        @Test
        void shouldReturnErrors_whenUrgencyConsiderationDateIsInPastForUrgentApplication() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
                                                        true, LocalDate.now().minusDays(1));

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);
            when(initiateGeneralAppService.validateUrgencyDates(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(URGENCY_DATE_CANNOT_BE_IN_PAST);
        }

        @Test
        void shouldNotCauseAnyErrors_whenUrgencyConsiderationDateIsInFutureForUrgentApplication() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
                                                        true, LocalDate.now());

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);
            when(initiateGeneralAppService.validateUrgencyDates(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotCauseAnyErrors_whenApplicationIsNotUrgentAndConsiderationDateIsNotProvided() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
                                                        false, null);

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);
            when(initiateGeneralAppService.validateUrgencyDates(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidEventForHearingScreenValidation extends GeneralAppSampleDataBuilder {

        private static final String VALIDATE_HEARING_PAGE = "ga-hearing-screen-validation";

        @Test
        void shouldNotReturnErrors_whenHearingDataIsNotPresent() {
            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        //Trial Dates validations
        @Test
        void shouldReturnErrors_whenTrialIsScheduledButTrialDateFromIsNull() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    null, null, true, getValidUnavailableDateList());
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(TRIAL_DATE_FROM_REQUIRED);
        }

        @Test
        void shouldReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedWithTrialDateToBeforeIt() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    LocalDate.now(), LocalDate.now().minusDays(1), true, getValidUnavailableDateList());
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(INVALID_TRIAL_DATE_RANGE);
        }

        @Test
        void shouldNotReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedWithNullTrialDateTo() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    LocalDate.now(), null, true, getValidUnavailableDateList());
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedWithTrialDateToAfterIt() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    LocalDate.now(), LocalDate.now().plusDays(1), true, getValidUnavailableDateList());
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedAndTrialDateToAreSame() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    LocalDate.now(), LocalDate.now(), true, getValidUnavailableDateList());
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnErrors_whenTrialIsNotScheduled() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), false,
                    null, null, true, getValidUnavailableDateList());
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        //Unavailability Dates validations
        @Test
        void shouldReturnErrors_whenUnavailabilityIsSetButNullDateRangeProvided() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    LocalDate.now(), null, true, null);
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(UNAVAILABLE_DATE_RANGE_MISSING);
        }

        @Test
        void shouldReturnErrors_whenUnavailabilityIsSetButDateRangeProvidedHasNullDateFrom() {
            GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
                    .unavailableTrialDateFrom(null)
                    .unavailableTrialDateTo(null)
                    .build();

            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    LocalDate.now(), null, true, wrapElements(range1));
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(UNAVAILABLE_FROM_MUST_BE_PROVIDED);
        }

        @Test
        void shouldReturnErrors_whenUnavailabilityIsSetButDateRangeProvidedHasDateFromAfterDateTo() {
            GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
                    .unavailableTrialDateFrom(LocalDate.now().plusDays(1))
                    .unavailableTrialDateTo(LocalDate.now())
                    .build();

            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    LocalDate.now(), null, true, wrapElements(range1));
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(INVALID_UNAVAILABILITY_RANGE);
        }

        @Test
        void shouldNotReturnErrors_whenUnavailabilityIsNotSet() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), false,
                    null, null, false, null);
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnErrors_whenUnavailabilityIsSetAndDateFromIsValidWithNullDateTo() {
            GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
                    .unavailableTrialDateFrom(LocalDate.now())
                    .unavailableTrialDateTo(null)
                    .build();

            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), false,
                    null, null, false, wrapElements(range1));
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnErrors_whenUnavailabilityIsSetAndDateFromIsValidWithSameDateTo() {
            GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
                    .unavailableTrialDateFrom(LocalDate.now())
                    .unavailableTrialDateTo(LocalDate.now())
                    .build();

            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), false,
                    null, null, false, wrapElements(range1));
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnErrors_whenUnavailabilityIsSetAndDateFromIsBeforeDateTo() {
            GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
                    .unavailableTrialDateFrom(LocalDate.now())
                    .unavailableTrialDateTo(LocalDate.now().plusDays(1))
                    .build();

            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), false,
                    null, null, false, wrapElements(range1));
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmit extends GeneralAppSampleDataBuilder {
        @Test
        void shouldAddNewApplicationToList_whenInvoked() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseData(CaseData.builder().build());

            when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder().id(STRING_CONSTANT)
                                                                      .email(APPLICANT_EMAIL_ID_CONSTANT)
                                                                      .build());
            when(initiateGeneralAppService.buildCaseData(any(CaseData.CaseDataBuilder.class),
                                                         any(CaseData.class), any(UserDetails.class)))
                .thenReturn(caseData);

            when(helper.setApplicantAndRespondentDetailsIfExits(any(GeneralApplication.class),
                                                                any(CaseData.class), any(UserDetails.class)))
                .thenReturn(GeneralApplicationDetailsBuilder.builder().getGeneralApplication());

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertResponse(objectMapper.convertValue(response.getData(), CaseData.class));
        }

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents()).contains(INITIATE_GENERAL_APPLICATION);
        }

        private void assertResponse(CaseData responseData) {
            assertThat(responseData)
                .extracting("generalApplications")
                .isNotNull();
            GeneralApplication application = unwrapElements(responseData.getGeneralApplications()).get(0);
            assertThat(application.getGeneralAppType().getTypes().contains(EXTEND_TIME)).isTrue();
            assertThat(application.getGeneralAppRespondentAgreement().getHasAgreed()).isEqualTo(NO);
            assertThat(application.getGeneralAppPBADetails().getApplicantsPbaAccounts())
                .isEqualTo(PBA_ACCOUNTS);
            assertThat(application.getGeneralAppDetailsOfOrder()).isEqualTo(STRING_CONSTANT);
            assertThat(application.getGeneralAppReasonsOfOrder()).isEqualTo(STRING_CONSTANT);
            assertThat(application.getGeneralAppInformOtherParty().getReasonsForWithoutNotice())
                .isEqualTo(STRING_CONSTANT);
            assertThat(application.getGeneralAppUrgencyRequirement().getUrgentAppConsiderationDate())
                .isEqualTo(APP_DATE_EPOCH);
            assertThat(application.getGeneralAppStatementOfTruth().getName()).isEqualTo(STRING_CONSTANT);
            assertThat(unwrapElements(application.getGeneralAppEvidenceDocument()).get(0).getDocumentUrl())
                .isEqualTo(STRING_CONSTANT);
            assertThat(application.getGeneralAppHearingDetails().getSupportRequirement()
                           .contains(OTHER_SUPPORT)).isTrue();
            assertThat(application.getGeneralAppHearingDetails().getHearingDuration()).isEqualTo(OTHER);
            assertThat(application.getGeneralAppHearingDetails().getGeneralAppHearingDays()).isEqualTo("1");
            assertThat(application.getGeneralAppHearingDetails().getGeneralAppHearingHours()).isEqualTo("2");
            assertThat(application.getGeneralAppHearingDetails().getGeneralAppHearingMinutes()).isEqualTo("30");
            assertThat(application.getGeneralAppHearingDetails().getHearingPreferencesPreferredType())
                .isEqualTo(IN_PERSON);
            assertThat(application.getIsMultiParty()).isEqualTo(NO);
            assertThat(application.getApplicantSolicitor1UserDetails().getEmail())
                .isEqualTo(APPLICANT_EMAIL_ID_CONSTANT);
            assertThat(application.getRespondentSolicitor1EmailAddress()).isEqualTo(RESPONDENT_EMAIL_ID_CONSTANT);
            assertThat(application.getApplicantSolicitor1UserDetails().getId()).isEqualTo(STRING_CONSTANT);
            assertThat(application.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID())
                .isEqualTo(STRING_CONSTANT);
            assertThat(application.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID())
                .isEqualTo(STRING_CONSTANT);
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnEmptyResponse_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualTo(SubmittedCallbackResponse.builder().build());
        }
    }
}
