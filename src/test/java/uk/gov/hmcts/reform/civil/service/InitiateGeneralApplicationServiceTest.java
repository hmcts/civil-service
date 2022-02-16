package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUnavailabilityDates;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.GeneralAppSampleDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
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
    InitiateGeneralApplicationService.class,
    JacksonAutoConfiguration.class,
})
class InitiateGeneralApplicationServiceTest extends GeneralAppSampleDataBuilder {

    public static final String APPLICANT_EMAIL_ID_CONSTANT = "testUser@gmail.com";
    public static final String RESPONDENT_EMAIL_ID_CONSTANT = "respondent@gmail.com";
    private static final LocalDateTime weekdayDate = LocalDate.of(2022, 2, 15).atTime(12, 0);

    @Autowired
    private InitiateGeneralApplicationService service;

    @MockBean
    private InitiateGeneralApplicationServiceHelper helper;

    @MockBean
    private GADeadlinesCalculator calc;

    @BeforeEach
    public void setUp() throws IOException {
        when(calc.calculateApplicantResponseDeadline(
            any(LocalDateTime.class),
            anyInt()))
            .thenReturn(weekdayDate);
    }

    @Test
    void shouldReturnCaseDataPopulated_whenValidApplicationIsBeingInitiated() {

        when(helper.setApplicantAndRespondentDetailsIfExits(any(GeneralApplication.class),
                                                            any(CaseData.class), any(UserDetails.class)))
            .thenReturn(GeneralApplicationDetailsBuilder.builder().getGeneralApplication());

        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithEmptyCollectionOfApps(CaseData.builder().build());

        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build());

        assertCollectionPopulated(result);
        assertCaseDateEntries(result);
    }

    @Test
    void shouldReturnCaseDataWithAdditionToCollection_whenAnotherApplicationIsBeingInitiated() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataCollectionOfApps(CaseData.builder().build());

        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build());

        assertThat(result.getGeneralApplications().size()).isEqualTo(2);
    }

    @Test
    void shouldReturnErrors_whenApplicationIsUrgentButConsiderationDateIsNotProvided() {
        GAUrgencyRequirement urgencyRequirement = GAUrgencyRequirement.builder()
                .generalAppUrgency(YES)
                .urgentAppConsiderationDate(null)
                .build();

        List<String> errors = service.validateUrgencyDates(urgencyRequirement);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(URGENCY_DATE_REQUIRED);
    }

    @Test
    void shouldReturnErrors_whenApplicationIsNotUrgentButConsiderationDateIsProvided() {
        GAUrgencyRequirement urgencyRequirement = GAUrgencyRequirement.builder()
                .generalAppUrgency(NO)
                .urgentAppConsiderationDate(LocalDate.now())
                .build();

        List<String> errors = service.validateUrgencyDates(urgencyRequirement);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(URGENCY_DATE_SHOULD_NOT_BE_PROVIDED);
    }

    @Test
    void shouldReturnErrors_whenUrgencyConsiderationDateIsInPastForUrgentApplication() {
        GAUrgencyRequirement urgencyRequirement = GAUrgencyRequirement.builder()
                .generalAppUrgency(YES)
                .urgentAppConsiderationDate(LocalDate.now().minusDays(1))
                .build();

        List<String> errors = service.validateUrgencyDates(urgencyRequirement);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(URGENCY_DATE_CANNOT_BE_IN_PAST);
    }

    @Test
    void shouldNotCauseAnyErrors_whenUrgencyConsiderationDateIsInFutureForUrgentApplication() {
        GAUrgencyRequirement urgencyRequirement = GAUrgencyRequirement.builder()
                .generalAppUrgency(YES)
                .urgentAppConsiderationDate(LocalDate.now())
                .build();

        List<String> errors = service.validateUrgencyDates(urgencyRequirement);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotCauseAnyErrors_whenApplicationIsNotUrgentAndConsiderationDateIsNotProvided() {
        GAUrgencyRequirement urgencyRequirement = GAUrgencyRequirement.builder()
                .generalAppUrgency(NO)
                .urgentAppConsiderationDate(null)
                .build();

        List<String> errors = service.validateUrgencyDates(urgencyRequirement);

        assertThat(errors).isEmpty();
    }

    //Trial Dates validations
    @Test
    void shouldReturnErrors_whenTrialIsScheduledButTrialDateFromIsNull() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
                .trialRequiredYesOrNo(YES)
                .trialDateFrom(null)
                .trialDateTo(null)
                .unavailableTrialRequiredYesOrNo(YES)
                .generalAppUnavailableDates(getValidUnavailableDateList())
                .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(TRIAL_DATE_FROM_REQUIRED);
    }

    @Test
    void shouldReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedWithTrialDateToBeforeIt() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
                .trialRequiredYesOrNo(YES)
                .trialDateFrom(LocalDate.now())
                .trialDateTo(LocalDate.now().minusDays(1))
                .unavailableTrialRequiredYesOrNo(YES)
                .generalAppUnavailableDates(getValidUnavailableDateList())
                .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(INVALID_TRIAL_DATE_RANGE);
    }

    @Test
    void shouldNotReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedWithNullTrialDateTo() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
                .trialRequiredYesOrNo(YES)
                .trialDateFrom(LocalDate.now())
                .trialDateTo(null)
                .unavailableTrialRequiredYesOrNo(YES)
                .generalAppUnavailableDates(getValidUnavailableDateList())
                .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedWithTrialDateToAfterIt() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
                .trialRequiredYesOrNo(YES)
                .trialDateFrom(LocalDate.now())
                .trialDateTo(LocalDate.now().plusDays(1))
                .unavailableTrialRequiredYesOrNo(YES)
                .generalAppUnavailableDates(getValidUnavailableDateList())
                .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedAndTrialDateToAreSame() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
                .trialRequiredYesOrNo(YES)
                .trialDateFrom(LocalDate.now())
                .trialDateTo(LocalDate.now())
                .unavailableTrialRequiredYesOrNo(YES)
                .generalAppUnavailableDates(getValidUnavailableDateList())
                .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrors_whenTrialIsNotScheduled() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
                .trialRequiredYesOrNo(NO)
                .trialDateFrom(null)
                .trialDateTo(null)
                .unavailableTrialRequiredYesOrNo(YES)
                .generalAppUnavailableDates(getValidUnavailableDateList())
                .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isEmpty();
    }

    //Unavailability Dates validations
    @Test
    void shouldReturnErrors_whenUnavailabilityIsSetButNullDateRangeProvided() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
                .trialRequiredYesOrNo(YES)
                .trialDateFrom(LocalDate.now())
                .trialDateTo(null)
                .unavailableTrialRequiredYesOrNo(YES)
                .generalAppUnavailableDates(null)
                .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(UNAVAILABLE_DATE_RANGE_MISSING);
    }

    @Test
    void shouldReturnErrors_whenUnavailabilityIsSetButDateRangeProvidedHasNullDateFrom() {
        GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
                .unavailableTrialDateFrom(null)
                .unavailableTrialDateTo(null)
                .build();

        GAHearingDetails hearingDetails = GAHearingDetails.builder()
                .trialRequiredYesOrNo(YES)
                .trialDateFrom(LocalDate.now())
                .trialDateTo(null)
                .unavailableTrialRequiredYesOrNo(YES)
                .generalAppUnavailableDates(wrapElements(range1))
                .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(UNAVAILABLE_FROM_MUST_BE_PROVIDED);
    }

    @Test
    void shouldReturnErrors_whenUnavailabilityIsSetButDateRangeProvidedHasDateFromAfterDateTo() {
        GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
                .unavailableTrialDateFrom(LocalDate.now().plusDays(1))
                .unavailableTrialDateTo(LocalDate.now())
                .build();

        GAHearingDetails hearingDetails = GAHearingDetails.builder()
                .trialRequiredYesOrNo(YES)
                .trialDateFrom(LocalDate.now())
                .trialDateTo(null)
                .unavailableTrialRequiredYesOrNo(YES)
                .generalAppUnavailableDates(wrapElements(range1))
                .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(INVALID_UNAVAILABILITY_RANGE);
    }

    @Test
    void shouldNotReturnErrors_whenUnavailabilityIsNotSet() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
                .trialRequiredYesOrNo(NO)
                .trialDateFrom(null)
                .trialDateTo(null)
                .unavailableTrialRequiredYesOrNo(NO)
                .generalAppUnavailableDates(null)
                .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrors_whenUnavailabilityIsSetAndDateFromIsValidWithNullDateTo() {
        GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
                .unavailableTrialDateFrom(LocalDate.now())
                .unavailableTrialDateTo(null)
                .build();

        GAHearingDetails hearingDetails = GAHearingDetails.builder()
                .trialRequiredYesOrNo(YES)
                .trialDateFrom(LocalDate.now())
                .trialDateTo(null)
                .unavailableTrialRequiredYesOrNo(NO)
                .generalAppUnavailableDates(wrapElements(range1))
                .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrors_whenUnavailabilityIsSetAndDateFromIsValidWithSameDateTo() {
        GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
                .unavailableTrialDateFrom(LocalDate.now())
                .unavailableTrialDateTo(LocalDate.now())
                .build();
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
                .trialRequiredYesOrNo(YES)
                .trialDateFrom(LocalDate.now())
                .trialDateTo(null)
                .unavailableTrialRequiredYesOrNo(NO)
                .generalAppUnavailableDates(wrapElements(range1))
                .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrors_whenUnavailabilityIsSetAndDateFromIsBeforeDateTo() {
        GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
                .unavailableTrialDateFrom(LocalDate.now())
                .unavailableTrialDateTo(LocalDate.now().plusDays(1))
                .build();
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
                .trialRequiredYesOrNo(YES)
                .trialDateFrom(LocalDate.now())
                .trialDateTo(null)
                .unavailableTrialRequiredYesOrNo(NO)
                .generalAppUnavailableDates(wrapElements(range1))
                .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturnDate_whenGeneralAppNotificationDeadlineIsInvoked() {
        String givenDate = GeneralApplication.builder()
            .generalAppDeadlineNotification(
                weekdayDate.toString())
            .build()
            .getGeneralAppDeadlineNotification();

        String actual = "2022-02-15T12:00";

        assertThat(givenDate).isNotNull();
        assertThat(givenDate).isEqualTo(actual);
    }

    private void assertCaseDateEntries(CaseData caseData) {
        assertThat(caseData.getGeneralAppType().getTypes()).isNull();
        assertThat(caseData.getGeneralAppRespondentAgreement().getHasAgreed()).isNull();
        assertThat(caseData.getGeneralAppPBADetails().getApplicantsPbaAccounts()).isNull();
        assertThat(caseData.getGeneralAppPBADetails().getPbaReference()).isNull();
        assertThat(caseData.getGeneralAppDetailsOfOrder()).isEmpty();
        assertThat(caseData.getGeneralAppReasonsOfOrder()).isEmpty();
        assertThat(caseData.getGeneralAppInformOtherParty().getIsWithNotice()).isNull();
        assertThat(caseData.getGeneralAppInformOtherParty().getReasonsForWithoutNotice()).isNull();
        assertThat(caseData.getGeneralAppUrgencyRequirement().getGeneralAppUrgency()).isNull();
        assertThat(caseData.getGeneralAppUrgencyRequirement().getReasonsForUrgency()).isNull();
        assertThat(caseData.getGeneralAppUrgencyRequirement().getUrgentAppConsiderationDate()).isNull();
        assertThat(caseData.getGeneralAppStatementOfTruth().getName()).isNull();
        assertThat(caseData.getGeneralAppStatementOfTruth().getRole()).isNull();
        assertThat(unwrapElements(caseData.getGeneralAppEvidenceDocument())).isEmpty();
        GAHearingDetails generalAppHearingDetails = caseData.getGeneralAppHearingDetails();
        assertThat(generalAppHearingDetails.getJudgeName()).isNull();
        assertThat(generalAppHearingDetails.getHearingDate()).isNull();
        assertThat(generalAppHearingDetails.getTrialDateFrom()).isNull();
        assertThat(generalAppHearingDetails.getTrialDateTo()).isNull();
        assertThat(generalAppHearingDetails.getHearingYesorNo()).isNull();
        assertThat(generalAppHearingDetails.getHearingDuration()).isNull();
        assertThat(generalAppHearingDetails.getSupportRequirement()).isNull();
        assertThat(generalAppHearingDetails.getJudgeRequiredYesOrNo()).isNull();
        assertThat(generalAppHearingDetails.getTrialRequiredYesOrNo()).isNull();
        assertThat(generalAppHearingDetails.getHearingDetailsEmailID()).isNull();
        assertThat(generalAppHearingDetails.getGeneralAppUnavailableDates()).isNull();
        assertThat(generalAppHearingDetails.getSupportRequirementOther()).isNull();
        assertThat(generalAppHearingDetails.getHearingDetailsTelephoneNumber()).isNull();
        assertThat(generalAppHearingDetails.getReasonForPreferredHearingType()).isNull();
        assertThat(generalAppHearingDetails.getTelephoneHearingPreferredType()).isNull();
        assertThat(generalAppHearingDetails.getSupportRequirementSignLanguage()).isNull();
        assertThat(generalAppHearingDetails.getHearingPreferencesPreferredType()).isNull();
        assertThat(generalAppHearingDetails.getUnavailableTrialRequiredYesOrNo()).isNull();
        assertThat(generalAppHearingDetails.getSupportRequirementLanguageInterpreter()).isNull();
    }

    private void assertCollectionPopulated(CaseData caseData) {
        assertThat(unwrapElements(caseData.getGeneralApplications()).size()).isEqualTo(1);
        GeneralApplication application = unwrapElements(caseData.getGeneralApplications()).get(0);

        assertThat(application.getGeneralAppType().getTypes().contains(EXTEND_TIME)).isTrue();
        assertThat(application.getGeneralAppRespondentAgreement().getHasAgreed()).isEqualTo(NO);
        assertThat(application.getGeneralAppPBADetails().getApplicantsPbaAccounts())
            .isEqualTo(PBALIST);
        assertThat(application.getGeneralAppPBADetails().getPbaReference())
            .isEqualTo(STRING_CONSTANT);
        assertThat(application.getGeneralAppDetailsOfOrder()).isEqualTo(STRING_CONSTANT);
        assertThat(application.getGeneralAppReasonsOfOrder()).isEqualTo(STRING_CONSTANT);
        assertThat(application.getGeneralAppInformOtherParty().getIsWithNotice())
            .isEqualTo(NO);
        assertThat(application.getGeneralAppInformOtherParty().getReasonsForWithoutNotice())
            .isEqualTo(STRING_CONSTANT);
        assertThat(application.getGeneralAppUrgencyRequirement().getGeneralAppUrgency())
            .isEqualTo(YES);
        assertThat(application.getGeneralAppUrgencyRequirement().getReasonsForUrgency())
            .isEqualTo(STRING_CONSTANT);
        assertThat(application.getGeneralAppUrgencyRequirement().getUrgentAppConsiderationDate())
            .isEqualTo(APP_DATE_EPOCH);
        assertThat(application.getGeneralAppStatementOfTruth().getName()).isEqualTo(STRING_CONSTANT);
        assertThat(application.getGeneralAppStatementOfTruth().getRole()).isEqualTo(STRING_CONSTANT);
        assertThat(unwrapElements(application.getGeneralAppEvidenceDocument()).get(0).getDocumentUrl())
            .isEqualTo(STRING_CONSTANT);
        assertThat(unwrapElements(application.getGeneralAppEvidenceDocument()).get(0).getDocumentHash())
            .isEqualTo(STRING_CONSTANT);
        assertThat(unwrapElements(application.getGeneralAppEvidenceDocument()).get(0).getDocumentBinaryUrl())
            .isEqualTo(STRING_CONSTANT);
        assertThat(unwrapElements(application.getGeneralAppEvidenceDocument()).get(0).getDocumentFileName())
            .isEqualTo(STRING_CONSTANT);
        GAHearingDetails generalAppHearingDetails = application.getGeneralAppHearingDetails();
        assertThat(generalAppHearingDetails.getJudgeName()).isEqualTo(STRING_CONSTANT);
        assertThat(generalAppHearingDetails.getHearingDate()).isEqualTo(APP_DATE_EPOCH);
        assertThat(generalAppHearingDetails.getTrialDateFrom()).isEqualTo(APP_DATE_EPOCH);
        assertThat(generalAppHearingDetails.getTrialDateTo()).isEqualTo(APP_DATE_EPOCH);
        assertThat(generalAppHearingDetails.getHearingYesorNo()).isEqualTo(YES);
        assertThat(generalAppHearingDetails.getHearingDuration()).isEqualTo(OTHER);
        assertThat(generalAppHearingDetails.getGeneralAppHearingDays()).isEqualTo("1");
        assertThat(generalAppHearingDetails.getGeneralAppHearingHours()).isEqualTo("2");
        assertThat(generalAppHearingDetails.getGeneralAppHearingMinutes()).isEqualTo("30");
        assertThat(generalAppHearingDetails.getSupportRequirement()
                       .contains(OTHER_SUPPORT)).isTrue();
        assertThat(generalAppHearingDetails.getJudgeRequiredYesOrNo()).isEqualTo(YES);
        assertThat(generalAppHearingDetails.getTrialRequiredYesOrNo()).isEqualTo(YES);
        assertThat(generalAppHearingDetails.getHearingDetailsEmailID()).isEqualTo(STRING_CONSTANT);
        assertThat(generalAppHearingDetails.getGeneralAppUnavailableDates().get(0).getValue()
                .getUnavailableTrialDateFrom()).isEqualTo(APP_DATE_EPOCH);
        assertThat(generalAppHearingDetails.getGeneralAppUnavailableDates().get(0).getValue()
                .getUnavailableTrialDateTo()).isEqualTo(APP_DATE_EPOCH);
        assertThat(generalAppHearingDetails.getSupportRequirementOther()).isEqualTo(STRING_CONSTANT);
        assertThat(generalAppHearingDetails.getHearingDetailsTelephoneNumber())
            .isEqualTo(STRING_NUM_CONSTANT);
        assertThat(generalAppHearingDetails.getReasonForPreferredHearingType()).isEqualTo(STRING_CONSTANT);
        assertThat(generalAppHearingDetails.getTelephoneHearingPreferredType()).isEqualTo(STRING_CONSTANT);
        assertThat(generalAppHearingDetails.getSupportRequirementSignLanguage()).isEqualTo(STRING_CONSTANT);
        assertThat(generalAppHearingDetails.getHearingPreferencesPreferredType())
            .isEqualTo(IN_PERSON);
        assertThat(generalAppHearingDetails.getUnavailableTrialRequiredYesOrNo()).isEqualTo(YES);
        assertThat(generalAppHearingDetails.getSupportRequirementLanguageInterpreter()).isEqualTo(STRING_CONSTANT);
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

