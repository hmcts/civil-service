package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NocNotificationUtilsTest {

    private CaseData caseData;
    private ChangeOfRepresentation change;

    private static final String TEST_EMAIL = "test@email.com";
    private static final String TEST_ORG = "123";
    private static final String PARTY_1_NAME = "party1";
    private static final String PARTY_2_NAME = "party2";
    private static final String TRUE_LIP_MESSAGE = "Should return true for LIP";

    @BeforeEach
    void setup() {
        change = ChangeOfRepresentation.builder()
            .formerRepresentationEmailAddress(TEST_EMAIL).build();
    }

    @Test
    void testGetPreviousSolicitorEmail() {
        caseData = CaseData.builder().changeOfRepresentation(change).build();
        assertEquals(TEST_EMAIL, NocNotificationUtils.getPreviousSolicitorEmail(caseData));
    }

    @Test
    void testGetOtherSolicitor1ReturnsApplicant1Details() {
        change.setCaseRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName());
        IdamUserDetails userDetails = IdamUserDetails.builder().email(TEST_EMAIL).build();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID(TEST_ORG).build())
            .build();
        caseData = CaseData.builder()
            .applicantSolicitor1UserDetails(userDetails)
            .changeOfRepresentation(change)
            .applicant1OrganisationPolicy(organisationPolicy)
            .build();

        assertEquals(TEST_EMAIL, NocNotificationUtils.getOtherSolicitor1Email(caseData));
        assertEquals(TEST_ORG, NocNotificationUtils.getOtherSolicitor1Name(caseData));
    }

    @Test
    void testGetOtherSolicitor1ReturnsRespondentSolicitor1Details() {
        change.setCaseRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName());
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID(TEST_ORG).build())
            .build();
        caseData = CaseData.builder()
            .respondentSolicitor1EmailAddress(TEST_EMAIL)
            .changeOfRepresentation(change)
            .respondent1OrganisationPolicy(organisationPolicy)
            .build();

        assertEquals(TEST_EMAIL, NocNotificationUtils.getOtherSolicitor1Email(caseData));
        assertEquals(TEST_ORG, NocNotificationUtils.getOtherSolicitor1Name(caseData));
    }

    @Test
    void testGetOtherSolicitor1ReturnsApplicant() {
        change.setCaseRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName());
        IdamUserDetails userDetails = IdamUserDetails.builder().email(TEST_EMAIL).build();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID(TEST_ORG).build())
            .build();
        caseData = CaseData.builder()
            .applicantSolicitor1UserDetails(userDetails)
            .changeOfRepresentation(change)
            .applicant1OrganisationPolicy(organisationPolicy)
            .build();

        assertEquals(TEST_EMAIL, NocNotificationUtils.getOtherSolicitor1Email(caseData));
        assertEquals(TEST_ORG, NocNotificationUtils.getOtherSolicitor1Name(caseData));
    }

    @Test
    void testisOtherParty1LipReturnsTrue() {
        change.setCaseRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName());
        caseData = CaseData.builder()
            .changeOfRepresentation(change)
            .build();

        assertTrue(NocNotificationUtils.isOtherParty1Lip(caseData), TRUE_LIP_MESSAGE);
    }

    @Test
    void testGetOtherSolicitor2ReturnsRespondent1Details() {
        change.setCaseRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName());
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID(TEST_ORG).build())
            .build();
        caseData = CaseData.builder()
            .respondentSolicitor1EmailAddress(TEST_EMAIL)
            .changeOfRepresentation(change)
            .respondent1OrganisationPolicy(organisationPolicy)
            .build();

        assertEquals(TEST_EMAIL, NocNotificationUtils.getOtherSolicitor2Email(caseData));
        assertEquals(TEST_ORG, NocNotificationUtils.getOtherSolicitor2Name(caseData));
    }

    @Test
    void testGetOtherSolicitor2ReturnsApplicant2_2v1() {
        change.setCaseRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName());
        IdamUserDetails userDetails = IdamUserDetails.builder().email(TEST_EMAIL).build();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID(TEST_ORG).build())
            .build();
        caseData = CaseData.builder()
            .applicantSolicitor1UserDetails(userDetails)
            .changeOfRepresentation(change)
            .addApplicant2(YesOrNo.YES)
            .applicant1OrganisationPolicy(organisationPolicy)
            .build();

        assertEquals(TEST_EMAIL, NocNotificationUtils.getOtherSolicitor2Email(caseData));
        assertEquals(TEST_ORG, NocNotificationUtils.getOtherSolicitor2Name(caseData));
    }

    @Test
    void testGetOtherSolicitor2ReturnsRespondentSolicitor2Details() {
        change.setCaseRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName());
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID(TEST_ORG).build())
            .build();
        caseData = CaseData.builder()
            .respondentSolicitor2EmailAddress(TEST_EMAIL)
            .changeOfRepresentation(change)
            .respondent2OrganisationPolicy(organisationPolicy)
            .build();

        assertEquals(TEST_EMAIL, NocNotificationUtils.getOtherSolicitor2Email(caseData));
        assertEquals(TEST_ORG, NocNotificationUtils.getOtherSolicitor2Name(caseData));
    }

    @Test
    void testIsOtherSolicitor2Lip_RespondentSolicitor1() {
        change.setCaseRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName());
        caseData = CaseData.builder()
            .changeOfRepresentation(change)
            .build();

        assertTrue(NocNotificationUtils.isOtherParty2Lip(caseData), TRUE_LIP_MESSAGE);
    }

    @Test
    void testIsOtherSolicitor2Lip_RespondentSolicitor2() {
        change.setCaseRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName());
        caseData = CaseData.builder()
            .changeOfRepresentation(change)
            .build();

        assertTrue(NocNotificationUtils.isOtherParty2Lip(caseData), TRUE_LIP_MESSAGE);
    }

    @Test
    void test1ApplicantV1Defendant() {
        Party applicant1 = Party.builder()
            .companyName(PARTY_1_NAME)
            .type(Party.Type.COMPANY).build();
        Party respondent1 = Party.builder()
            .companyName(PARTY_2_NAME)
            .type(Party.Type.COMPANY).build();
        caseData = CaseData.builder()
            .applicant1(applicant1)
            .respondent1(respondent1)
            .build();

        assertEquals(String.format("%s v %s", PARTY_1_NAME, PARTY_2_NAME), NocNotificationUtils.getCaseName(caseData));
    }

    @Test
    void test2ApplicantV1Defendant() {
        Party applicant1 = Party.builder()
            .companyName(PARTY_1_NAME)
            .type(Party.Type.COMPANY).build();
        Party respondent1 = Party.builder()
            .companyName(PARTY_2_NAME)
            .type(Party.Type.COMPANY).build();
        caseData = CaseData.builder()
            .applicant1(applicant1)
            .applicant2(applicant1)
            .respondent1(respondent1)
            .build();

        assertEquals(String.format("%s, %s v %s", PARTY_1_NAME, PARTY_1_NAME, PARTY_2_NAME),
                     NocNotificationUtils.getCaseName(caseData));
    }

    @Test
    void test1ApplicantV2Defendant() {
        Party applicant1 = Party.builder()
            .companyName(PARTY_1_NAME)
            .type(Party.Type.COMPANY).build();
        Party respondent1 = Party.builder()
            .companyName(PARTY_2_NAME)
            .type(Party.Type.COMPANY).build();
        caseData = CaseData.builder()
            .applicant1(applicant1)
            .respondent1(respondent1)
            .respondent2(respondent1)
            .build();

        assertEquals(String.format("%s v %s, %s", PARTY_1_NAME, PARTY_2_NAME, PARTY_2_NAME),
                     NocNotificationUtils.getCaseName(caseData));
    }

    @Test
    void test2ApplicantV2Defendant() {
        Party applicant1 = Party.builder()
            .companyName(PARTY_1_NAME)
            .type(Party.Type.COMPANY).build();
        Party respondent1 = Party.builder()
            .companyName(PARTY_2_NAME)
            .type(Party.Type.COMPANY).build();
        caseData = CaseData.builder()
            .applicant1(applicant1)
            .applicant2(applicant1)
            .respondent1(respondent1)
            .respondent2(respondent1)
            .build();

        assertEquals(String.format("%s, %s v %s, %s", PARTY_1_NAME, PARTY_1_NAME, PARTY_2_NAME, PARTY_2_NAME),
                     NocNotificationUtils.getCaseName(caseData));
    }

    @Test
    void testGetCaseDataWithoutFormerSolicitorEmail() {
        CaseData caseData = CaseData.builder()
            .changeOfRepresentation(
                ChangeOfRepresentation.builder()
                    .formerRepresentationEmailAddress("former-sol-email")
                    .organisationToAddID("some org")
                    .build()).build();

        CaseData expected = CaseData.builder()
            .changeOfRepresentation(
                ChangeOfRepresentation.builder()
                    .organisationToAddID("some org")
                    .build()).build();

        CaseData actual = NocNotificationUtils.getCaseDataWithoutFormerSolicitorEmail(caseData);

        assertEquals(expected, actual);
    }
}
