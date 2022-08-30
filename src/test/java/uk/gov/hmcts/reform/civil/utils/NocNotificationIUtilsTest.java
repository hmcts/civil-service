package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NocNotificationIUtilsTest {

    private CaseData caseData;
    private ChangeOfRepresentation change;

    private static final String TEST_EMAIL = "test@email.com";
    private static final String TEST_ORG = "123";
    private static final String PARTY_1_NAME = "party1";
    private static final String PARTY_2_NAME = "party2";

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
    void testGetOtherSol1ReturnsApplicantSol1Details() {
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
    void testGetOtherSol1ReturnsRespondent2Details() {
        change.setCaseRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName());
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID(TEST_ORG).build())
            .build();
        caseData = CaseData.builder()
            .respondentSolicitor2EmailAddress(TEST_EMAIL)
            .changeOfRepresentation(change)
            .respondent2OrganisationPolicy(organisationPolicy)
            .build();

        assertEquals(TEST_EMAIL, NocNotificationUtils.getOtherSolicitor1Email(caseData));
        assertEquals(TEST_ORG, NocNotificationUtils.getOtherSolicitor1Name(caseData));
    }

    @Test
    void testGetOtherSol1ReturnsRespondent1Details() {
        change.setCaseRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName());
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
    void testGetOtherSol2ReturnsRespondent1Details() {
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
    void testGetOtherSol2ReturnsRespondent2Details() {
        change.setCaseRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName());
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
}
