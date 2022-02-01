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
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUnavailabilityDates;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.LocalDate.EPOCH;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration.HOUR_1;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingSupportRequirements.OTHER_SUPPORT;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingType.IN_PERSON;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME;
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
    private OrganisationService organisationService;

    private static final String STRING_CONSTANT = "this is a string";
    private static final String STRING_NUM_CONSTANT = "123456789";
    private static final DynamicList PBA_ACCOUNTS = DynamicList.builder().build();
    private static final LocalDate APP_DATE_EPOCH = EPOCH;

    private CaseData getTestCaseData(CaseData caseData) {
        return caseData.toBuilder()
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(NO)
                                               .build())
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .applicantsPbaAccounts(PBA_ACCOUNTS)
                                      .pbaReference(STRING_CONSTANT)
                                      .build())
            .generalAppDetailsOfOrder(STRING_CONSTANT)
            .generalAppReasonsOfOrder(STRING_CONSTANT)
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(NO)
                                            .reasonsForWithoutNotice(STRING_CONSTANT)
                                            .build())
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder()
                                              .generalAppUrgency(YES)
                                              .reasonsForUrgency(STRING_CONSTANT)
                                              .urgentAppConsiderationDate(APP_DATE_EPOCH)
                                              .build())
            .generalAppStatementOfTruth(GAStatementOfTruth.builder()
                                            .name(STRING_CONSTANT)
                                            .role(STRING_CONSTANT)
                                            .build())
            .generalAppEvidenceDocument(wrapElements(Document.builder().documentUrl(STRING_CONSTANT).build()))
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .judgeName(STRING_CONSTANT)
                                          .hearingDate(APP_DATE_EPOCH)
                                          .trialDateFrom(APP_DATE_EPOCH)
                                          .trialDateTo(APP_DATE_EPOCH)
                                          .hearingYesorNo(YES)
                                          .hearingDuration(HOUR_1)
                                          .supportRequirement(singletonList(OTHER_SUPPORT))
                                          .judgeRequiredYesOrNo(YES)
                                          .trialRequiredYesOrNo(YES)
                                          .hearingDetailsEmailID(STRING_CONSTANT)
                                          .supportRequirementOther(STRING_CONSTANT)
                                          .hearingPreferredLocation(DynamicList.builder().build())
                                          .generalAppUnavailableDates(wrapElements(GAUnavailabilityDates.builder()
                                                  .unavailableTrialDateFrom(APP_DATE_EPOCH)
                                                  .unavailableTrialDateTo(APP_DATE_EPOCH).build()))
                                          .hearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                          .reasonForPreferredHearingType(STRING_CONSTANT)
                                          .telephoneHearingPreferredType(STRING_CONSTANT)
                                          .supportRequirementSignLanguage(STRING_CONSTANT)
                                          .hearingPreferencesPreferredType(IN_PERSON)
                                          .unavailableTrialRequiredYesOrNo(YES)
                                          .supportRequirementLanguageInterpreter(STRING_CONSTANT)
                                          .build())
            .build();
    }

    private CaseData getTestCaseDataForUrgencyCheckMidEvent(CaseData caseData, boolean isApplicationUrgent,
                                                            LocalDate urgencyConsiderationDate) {
        GAUrgencyRequirement.GAUrgencyRequirementBuilder urBuilder = GAUrgencyRequirement.builder();
        if (isApplicationUrgent) {
            urBuilder.generalAppUrgency(YES)
                    .reasonsForUrgency(STRING_CONSTANT);
        } else {
            urBuilder.generalAppUrgency(NO);
        }
        urBuilder.urgentAppConsiderationDate(urgencyConsiderationDate);
        GAUrgencyRequirement gaUrgencyRequirement = urBuilder.build();
        return caseData.toBuilder()
                .generalAppType(GAApplicationType.builder()
                        .types(singletonList(EXTEND_TIME))
                        .build())
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                        .hasAgreed(NO)
                        .build())
                .generalAppPBADetails(GAPbaDetails.builder()
                        .applicantsPbaAccounts(PBA_ACCOUNTS)
                        .pbaReference(STRING_CONSTANT)
                        .build())
                .generalAppDetailsOfOrder(STRING_CONSTANT)
                .generalAppReasonsOfOrder(STRING_CONSTANT)
                .generalAppInformOtherParty(GAInformOtherParty.builder()
                        .isWithNotice(NO)
                        .reasonsForWithoutNotice(STRING_CONSTANT)
                        .build())
                .generalAppUrgencyRequirement(gaUrgencyRequirement)
                .generalAppStatementOfTruth(GAStatementOfTruth.builder()
                        .name(STRING_CONSTANT)
                        .role(STRING_CONSTANT)
                        .build())
                .generalAppEvidenceDocument(wrapElements(Document.builder().documentUrl(STRING_CONSTANT).build()))
                .generalAppHearingDetails(GAHearingDetails.builder()
                        .judgeName(STRING_CONSTANT)
                        .hearingDate(APP_DATE_EPOCH)
                        .trialDateFrom(APP_DATE_EPOCH)
                        .trialDateTo(APP_DATE_EPOCH)
                        .hearingYesorNo(YES)
                        .hearingDuration(HOUR_1)
                        .supportRequirement(singletonList(OTHER_SUPPORT))
                        .judgeRequiredYesOrNo(YES)
                        .trialRequiredYesOrNo(YES)
                        .hearingDetailsEmailID(STRING_CONSTANT)
                        .generalAppUnavailableDates(wrapElements(GAUnavailabilityDates.builder()
                                .unavailableTrialDateFrom(APP_DATE_EPOCH)
                                .unavailableTrialDateTo(APP_DATE_EPOCH).build()))
                        .supportRequirementOther(STRING_CONSTANT)
                        .hearingPreferredLocation(DynamicList.builder().build())
                        .hearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                        .reasonForPreferredHearingType(STRING_CONSTANT)
                        .telephoneHearingPreferredType(STRING_CONSTANT)
                        .supportRequirementSignLanguage(STRING_CONSTANT)
                        .hearingPreferencesPreferredType(IN_PERSON)
                        .unavailableTrialRequiredYesOrNo(YES)
                        .supportRequirementLanguageInterpreter(STRING_CONSTANT)
                        .build())
                .build();
    }

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
            CaseData caseData = getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
                    true, null);

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains("Details of urgency consideration date required.");
        }

        @Test
        void shouldReturnErrors_whenApplicationIsNotUrgentButConsiderationDateIsProvided() {
            CaseData caseData = getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
                    false, LocalDate.now());

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(
                    "Urgency consideration date should not be provided for a non-urgent application.");
        }

        @Test
        void shouldReturnErrors_whenUrgencyConsiderationDateIsInPastForUrgentApplication() {
            CaseData caseData = getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
                    true, LocalDate.now().minusDays(1));

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(
                    "The date entered cannot be in the past.");
        }

        @Test
        void shouldNotCauseAnyErrors_whenUrgencyConsiderationDateIsInFutureForUrgentApplication() {
            CaseData caseData = getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
                    true, LocalDate.now());

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotCauseAnyErrors_whenApplicationIsNotUrgentAndConsiderationDateIsNotProvided() {
            CaseData caseData = getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
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
            when(initiateGeneralAppService.buildCaseData(any(CaseData.CaseDataBuilder.class), any(CaseData.class)))
                .thenCallRealMethod();
            CaseData caseData = getTestCaseData(CaseDataBuilder.builder().build());

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
