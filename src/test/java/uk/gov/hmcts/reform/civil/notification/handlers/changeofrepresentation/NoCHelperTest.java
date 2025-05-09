package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.RecipientData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CCD_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_16_DIGIT_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.COURT_LOCATION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FORMER_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DUE_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_FEE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_TIME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUE_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_REP_NAME_WITH_SPACE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.NEW_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OTHER_SOL_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REFERENCE;

@ExtendWith(MockitoExtension.class)
@ExtendWith(MockitoExtension.class)
class NoCHelperTest {

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private NoCHelper noCHelper;

    private CaseData baseCaseData;

    @BeforeEach
    void setUp() {
        baseCaseData = CaseData.builder()
            .ccdCaseReference(1234567890123456L)
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualFirstName("Applicant")
                            .individualLastName("A")
                            .partyName("Applicant A").build())
            .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                              .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder().organisationID("QWERTY A").build())
                                              .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualFirstName("Respondent")
                             .individualLastName("A")
                             .partyName("Respondent A").build())
            .respondent2(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualFirstName("Respondent")
                             .individualLastName("B")
                             .partyName("Respondent B").build())
            .legacyCaseReference("LEGACY-REF")
            .issueDate(LocalDate.of(2024, 5, 1))
            .changeOfRepresentation(ChangeOfRepresentation.builder()
                                        .caseRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                                        .organisationToAddID("orgAdd")
                                        .organisationToRemoveID("orgRemove")
                                        .formerRepresentationEmailAddress("former@sol.com")
                                        .build())
            .applicant1Represented(YesOrNo.YES)
            .hearingDate(LocalDate.of(2024, 6, 1))
            .hearingDueDate(LocalDate.of(2024, 5, 20))
            .hearingTimeHourMinute("10:30")
            .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(10000)).build())
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("Court A").build()).build())
            .hearingFeePaymentDetails(PaymentDetails.builder().status(PaymentStatus.SUCCESS).build())
            .build();
    }

    @Test
    void getProperties_shouldReturnExpectedMap() {

        when(organisationService.findOrganisationById("QWERTY A"))
            .thenReturn(Optional.of(Organisation.builder().name("App Legal Org").build()));

        when(organisationService.findOrganisationById("orgAdd"))
            .thenReturn(Optional.of(Organisation.builder().name("New Org").build()));

        when(organisationService.findOrganisationById("orgRemove"))
            .thenReturn(Optional.of(Organisation.builder().name("Old Org").build()));

        Map<String, String> props = noCHelper.getProperties(baseCaseData, false);

        assertThat(props)
            .containsEntry(CASE_NAME, "Applicant A v Respondent A, Respondent B")
            .containsEntry(ISSUE_DATE, "1 May 2024")
            .containsEntry(CCD_REF, "1234567890123456")
            .containsEntry(FORMER_SOL, "Old Org")
            .containsEntry(NEW_SOL, "New Org")
            .containsEntry(OTHER_SOL_NAME, "App Legal Org")
            .containsEntry(LEGAL_REP_NAME_WITH_SPACE, "New Org")
            .containsEntry(REFERENCE, "1234567890123456");
    }

    @Test
    void getClaimantLipProperties_shouldReturnExpectedMap() {
        Map<String, String> props = noCHelper.getClaimantLipProperties(baseCaseData);

        assertThat(props)
            .containsEntry(CLAIMANT_NAME, "Applicant A")
            .containsEntry(DEFENDANT_NAME_INTERIM, "Respondent A")
            .containsEntry(CLAIM_NUMBER, "LEGACY-REF")
            .containsEntry(CLAIM_16_DIGIT_NUMBER, "1234567890123456");
    }

    @Test
    void getHearingFeeEmailProperties_shouldReturnExpectedMap() {
        when(organisationService.findOrganisationById("QWERTY A"))
            .thenReturn(Optional.of(Organisation.builder().name("App Legal Org").build()));

        Map<String, String> props = noCHelper.getHearingFeeEmailProperties(baseCaseData);

        assertThat(props)
            .containsEntry(LEGAL_ORG_NAME, "App Legal Org")
            .containsEntry(HEARING_DATE, "1 June 2024")
            .containsEntry(COURT_LOCATION, "Court A")
            .containsEntry(HEARING_TIME, "10:30")
            .containsEntry(HEARING_FEE, "£100.00")
            .containsEntry(HEARING_DUE_DATE, "20 May 2024");
    }

    @Test
    void isHearingFeePaid_shouldReturnTrueWhenSuccess() {
        assertTrue(noCHelper.isHearingFeePaid(baseCaseData));
    }

    @Test
    void getCaseName_shouldHandleMultipleRespondents() {
        String name = noCHelper.getCaseName(baseCaseData);
        assertEquals("Applicant A v Respondent A, Respondent B", name);
    }

    @Test
    void getOtherSolicitor1And2NameAndEmail_shouldReturnNullWhenLiP() {
        assertEquals("QWERTY A", noCHelper.getOtherSolicitor1Name(baseCaseData));
        assertNull(noCHelper.getOtherSolicitor2Name(baseCaseData));
        assertNull(noCHelper.getOtherSolicitor1Email(baseCaseData));
        assertNull(noCHelper.getOtherSolicitor2Email(baseCaseData));
    }

    @Test
    void isOtherParty1Lip_shouldReturnFalse() {
        assertFalse(noCHelper.isOtherParty1Lip(baseCaseData));
    }

    @Test
    void isOtherParty2Lip_shouldReturnTrue() {
        assertTrue(noCHelper.isOtherParty2Lip(baseCaseData));
    }

    @Test
    void getPreviousSolicitorEmail_shouldReturnCorrectEmail() {
        assertEquals("former@sol.com", noCHelper.getPreviousSolicitorEmail(baseCaseData));
    }

    @Test
    void isApplicantLipForRespondentSolicitorChange_shouldReturnTrue() {
        baseCaseData = baseCaseData.toBuilder()
            .respondent2(null)
            .applicant1Represented(YesOrNo.NO).build();
        assertTrue(noCHelper.isApplicantLipForRespondentSolicitorChange(baseCaseData));
    }

    @Test
    void isOtherPartyLip_shouldReturnTrueWhenOrgPolicyNull() {
        assertTrue(noCHelper.isOtherPartyLip(null));
    }

    @Test
    void getOtherSolicitor2_shouldReturnRespondent1SolicitorData_whenRespondent2IsNewSolicitor_andRespondent1IsNotLip() {
        OrganisationPolicy respondent1Policy = OrganisationPolicy.builder()
            .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                              .organisationID("RESP1_ORG_ID").build())
            .build();

        CaseData caseData = baseCaseData.toBuilder()
            .respondent1OrganisationPolicy(respondent1Policy)
            .respondentSolicitor1EmailAddress("resp1sol@example.com")
            .changeOfRepresentation(ChangeOfRepresentation.builder()
                                        .caseRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                                        .organisationToAddID("orgAdd")
                                        .organisationToRemoveID("orgRemove")
                                        .formerRepresentationEmailAddress("former@sol.com")
                                        .build())
            .build();

        RecipientData result = noCHelper.getOtherSolicitor2(caseData);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("resp1sol@example.com");
        assertThat(result.getOrgId()).isEqualTo("RESP1_ORG_ID");
    }

    @Test
    void getOtherSolicitor2_shouldReturnApplicantSolicitorData_whenScenarioIsTwoVOne() {
        OrganisationPolicy applicantPolicy = OrganisationPolicy.builder()
            .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                              .organisationID("APP_ORG_ID").build())
            .build();

        CaseData caseData = baseCaseData.toBuilder()
            .applicant1OrganisationPolicy(applicantPolicy)
            .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                .email("appsol@example.com").build())
            .addApplicant2(YesOrNo.YES)
            .build();

        RecipientData result = noCHelper.getOtherSolicitor2(caseData);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("appsol@example.com");
        assertThat(result.getOrgId()).isEqualTo("APP_ORG_ID");
    }

    @Test
    void getOtherSolicitor2_shouldReturnRespondent2SolicitorData_whenRespondent2NotLipAndNotTwoVOne() {
        OrganisationPolicy respondent2Policy = OrganisationPolicy.builder()
            .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                              .organisationID("RESP2_ORG_ID").build())
            .build();

        CaseData caseData = baseCaseData.toBuilder()
            .respondent2OrganisationPolicy(respondent2Policy)
            .respondentSolicitor2EmailAddress("resp2sol@example.com")
            .build();

        RecipientData result = noCHelper.getOtherSolicitor2(caseData);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("resp2sol@example.com");
        assertThat(result.getOrgId()).isEqualTo("RESP2_ORG_ID");
    }

    @Test
    void getOtherSolicitor2_shouldReturnNull_whenAllConditionsFail() {
        CaseData caseData = baseCaseData;

        RecipientData result = noCHelper.getOtherSolicitor2(caseData);

        assertThat(result).isNull();
    }

    @Test
    void getOtherSolicitor1_shouldReturnRespondent1Solicitor_whenApplicant1IsNewSolicitor_andRespondent1IsNotLip() {
        OrganisationPolicy respondent1Policy = OrganisationPolicy.builder()
            .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                              .organisationID("RESP1_ORG_ID").build())
            .build();

        CaseData caseData = baseCaseData.toBuilder()
            .respondent1OrganisationPolicy(respondent1Policy)
            .respondentSolicitor1EmailAddress("resp1sol@example.com")
            .changeOfRepresentation(ChangeOfRepresentation.builder()
                                        .caseRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                                        .organisationToAddID("orgAdd")
                                        .organisationToRemoveID("orgRemove")
                                        .formerRepresentationEmailAddress("former@sol.com")
                                        .build())
            .build();

        RecipientData result = noCHelper.getOtherSolicitor1(caseData);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("resp1sol@example.com");
        assertThat(result.getOrgId()).isEqualTo("RESP1_ORG_ID");
    }

    @Test
    void getOtherSolicitor1_shouldReturnApplicantSolicitor_whenRespondent2IsNewSolicitor() {
        OrganisationPolicy applicantPolicy = OrganisationPolicy.builder()
            .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                              .organisationID("APP_ORG_ID").build())
            .build();

        CaseData caseData = baseCaseData.toBuilder()
            .applicant1OrganisationPolicy(applicantPolicy)
            .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                .email("appsol@example.com").build())
            .changeOfRepresentation(ChangeOfRepresentation.builder()
                                        .caseRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                                        .organisationToAddID("orgAdd")
                                        .organisationToRemoveID("orgRemove")
                                        .formerRepresentationEmailAddress("former@sol.com")
                                        .build())
            .build();

        RecipientData result = noCHelper.getOtherSolicitor1(caseData);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("appsol@example.com");
        assertThat(result.getOrgId()).isEqualTo("APP_ORG_ID");
    }

    @Test
    void getOtherSolicitor1_shouldReturnApplicantSolicitor_whenRespondent1IsNewSolicitor_andApplicantIsNotLip() {
        OrganisationPolicy applicantPolicy = OrganisationPolicy.builder()
            .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                              .organisationID("APP_ORG_ID").build())
            .build();

        CaseData caseData = baseCaseData.toBuilder()
            .applicant1OrganisationPolicy(applicantPolicy)
            .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                .email("appsol@example.com").build())
            .changeOfRepresentation(ChangeOfRepresentation.builder()
                                        .caseRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                                        .organisationToAddID("orgAdd")
                                        .organisationToRemoveID("orgRemove")
                                        .formerRepresentationEmailAddress("former@sol.com")
                                        .build())
            .applicant1Represented(YesOrNo.YES)
            .build();

        RecipientData result = noCHelper.getOtherSolicitor1(caseData);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("appsol@example.com");
        assertThat(result.getOrgId()).isEqualTo("APP_ORG_ID");
    }

    @Test
    void getOtherSolicitor1_shouldReturnNull_whenNoConditionsMatch() {
        CaseData caseData = baseCaseData.toBuilder()
            .changeOfRepresentation(ChangeOfRepresentation.builder()
                                        .caseRole(CaseRole.CLAIMANT.getFormattedName())
                                        .build())
            .build();

        RecipientData result = noCHelper.getOtherSolicitor1(caseData);

        assertThat(result).isNull();
    }
}
