package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.OrganisationPolicy;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationHelperTest {

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
                                              .organisation(uk.gov.hmcts.reform.civil.model.Organisation.builder().organisationID("QWERTY A").build())
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
    void getCaseName_shouldHandleMultipleRespondents() {
        String name = NotificationHelper.getCaseName(baseCaseData);
        assertEquals("Applicant A v Respondent A, Respondent B", name);
    }

    @Test
    void getOtherSolicitor1And2NameAndEmail_shouldReturnNullWhenLiP() {
        assertEquals("QWERTY A", NotificationHelper.getOtherSolicitor1Name(baseCaseData));
        assertNull(NotificationHelper.getOtherSolicitor2Name(baseCaseData));
        assertNull(NotificationHelper.getOtherSolicitor1Email(baseCaseData));
        assertNull(NotificationHelper.getOtherSolicitor2Email(baseCaseData));
    }

    @Test
    void isOtherParty1Lip_shouldReturnFalse() {
        assertFalse(NotificationHelper.isOtherParty1Lip(baseCaseData));
    }

    @Test
    void isOtherParty2Lip_shouldReturnTrue() {
        assertTrue(NotificationHelper.isOtherParty2Lip(baseCaseData));
    }

    @Test
    void getPreviousSolicitorEmail_shouldReturnCorrectEmail() {
        assertEquals("former@sol.com", NotificationHelper.getPreviousSolicitorEmail(baseCaseData));
    }

    @Test
    void isApplicantLipForRespondentSolicitorChange_shouldReturnTrue() {
        baseCaseData = baseCaseData.toBuilder()
            .respondent2(null)
            .applicant1Represented(YesOrNo.NO).build();
        assertTrue(NotificationHelper.isApplicantLipForRespondentSolicitorChange(baseCaseData));
    }

    @Test
    void isOtherPartyLip_shouldReturnTrueWhenOrgPolicyNull() {
        assertTrue(NotificationHelper.isOtherPartyLip(null));
    }

    @Test
    void getOtherSolicitor2_shouldReturnRespondent1SolicitorData_whenRespondent2IsNewSolicitor_andRespondent1IsNotLip() {
        OrganisationPolicy respondent1Policy = OrganisationPolicy.builder()
            .organisation(uk.gov.hmcts.reform.civil.model.Organisation.builder()
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

        RecipientData result = NotificationHelper.getOtherSolicitor2(caseData);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("resp1sol@example.com");
        assertThat(result.getOrgId()).isEqualTo("RESP1_ORG_ID");
    }

    @Test
    void getOtherSolicitor2_shouldReturnApplicantSolicitorData_whenScenarioIsTwoVOne() {
        OrganisationPolicy applicantPolicy = OrganisationPolicy.builder()
            .organisation(uk.gov.hmcts.reform.civil.model.Organisation.builder()
                              .organisationID("APP_ORG_ID").build())
            .build();

        CaseData caseData = baseCaseData.toBuilder()
            .applicant1OrganisationPolicy(applicantPolicy)
            .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                .email("appsol@example.com").build())
            .addApplicant2(YesOrNo.YES)
            .build();

        RecipientData result = NotificationHelper.getOtherSolicitor2(caseData);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("appsol@example.com");
        assertThat(result.getOrgId()).isEqualTo("APP_ORG_ID");
    }

    @Test
    void getOtherSolicitor2_shouldReturnRespondent2SolicitorData_whenRespondent2NotLipAndNotTwoVOne() {
        OrganisationPolicy respondent2Policy = OrganisationPolicy.builder()
            .organisation(uk.gov.hmcts.reform.civil.model.Organisation.builder()
                              .organisationID("RESP2_ORG_ID").build())
            .build();

        CaseData caseData = baseCaseData.toBuilder()
            .respondent2OrganisationPolicy(respondent2Policy)
            .respondentSolicitor2EmailAddress("resp2sol@example.com")
            .build();

        RecipientData result = NotificationHelper.getOtherSolicitor2(caseData);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("resp2sol@example.com");
        assertThat(result.getOrgId()).isEqualTo("RESP2_ORG_ID");
    }

    @Test
    void getOtherSolicitor2_shouldReturnNull_whenAllConditionsFail() {
        CaseData caseData = baseCaseData;

        RecipientData result = NotificationHelper.getOtherSolicitor2(caseData);

        assertThat(result).isNull();
    }

    @Test
    void getOtherSolicitor1_shouldReturnRespondent1Solicitor_whenApplicant1IsNewSolicitor_andRespondent1IsNotLip() {
        OrganisationPolicy respondent1Policy = OrganisationPolicy.builder()
            .organisation(uk.gov.hmcts.reform.civil.model.Organisation.builder()
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

        RecipientData result = NotificationHelper.getOtherSolicitor1(caseData);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("resp1sol@example.com");
        assertThat(result.getOrgId()).isEqualTo("RESP1_ORG_ID");
    }

    @Test
    void getOtherSolicitor1_shouldReturnApplicantSolicitor_whenRespondent2IsNewSolicitor() {
        OrganisationPolicy applicantPolicy = OrganisationPolicy.builder()
            .organisation(uk.gov.hmcts.reform.civil.model.Organisation.builder()
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

        RecipientData result = NotificationHelper.getOtherSolicitor1(caseData);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("appsol@example.com");
        assertThat(result.getOrgId()).isEqualTo("APP_ORG_ID");
    }

    @Test
    void getOtherSolicitor1_shouldReturnApplicantSolicitor_whenRespondent1IsNewSolicitor_andApplicantIsNotLip() {
        OrganisationPolicy applicantPolicy = OrganisationPolicy.builder()
            .organisation(uk.gov.hmcts.reform.civil.model.Organisation.builder()
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

        RecipientData result = NotificationHelper.getOtherSolicitor1(caseData);

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

        RecipientData result = NotificationHelper.getOtherSolicitor1(caseData);

        assertThat(result).isNull();
    }
}
