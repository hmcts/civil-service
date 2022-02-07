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
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration.HOUR_1;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingSupportRequirements.OTHER_SUPPORT;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingType.IN_PERSON;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

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
    class AboutToStartCallback {

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
    class MidEventForUrgencyCheck {

        private static final String VALIDATE_URGENCY_DATE_PAGE = "ga-validate-urgency-date";

        @Test
        void shouldReturnErrors_whenApplicationIsUrgentButConsiderationDateIsNotProvided() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
                                                        true, null);

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains("Details of urgency consideration date required.");
        }

        @Test
        void shouldReturnErrors_whenApplicationIsNotUrgentButConsiderationDateIsProvided() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
                                                        false, LocalDate.now());

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(
                    "Urgency consideration date should not be provided for a non-urgent application.");
        }

        @Test
        void shouldReturnErrors_whenUrgencyConsiderationDateIsInPastForUrgentApplication() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
                                                        true, LocalDate.now().minusDays(1));

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(
                    "The date entered cannot be in the past.");
        }

        @Test
        void shouldNotCauseAnyErrors_whenUrgencyConsiderationDateIsInFutureForUrgentApplication() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
                                                        true, LocalDate.now());

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotCauseAnyErrors_whenApplicationIsNotUrgentAndConsiderationDateIsNotProvided() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
                                                        false, null);

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmit {
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
            assertThat(application.getGeneralAppHearingDetails().getHearingDuration()).isEqualTo(HOUR_1);
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
