package uk.gov.hmcts.reform.civil.service.mediation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MediationCSVLipVLipServiceTest {

    @InjectMocks
    private MediationCSVLipVLipService service;

    private static final String APPLICANT_COMPANY_NAME = "Applicant company name";
    private static final String APPLICANT_ORGANISATION_NAME = "Applicant organisation name";
    private static final String APPLICANT_EMAIL_ADDRESS = "Applicant@company.com";
    private static final String APPLICANT_PHONE_NUMBER = "7553072111";
    private static final String APPLICANT_INDIVIDUAL_FIRST_NAME = "Applicant Individual First Name";
    private static final String APPLICANT_INDIVIDUAL_LAST_NAME = "Applicant Individual Last Name";
    private static final String APPLICANT_SOLE_TRADER_FIRST_NAME = "Applicant Sole Trader First Name";
    private static final String APPLICANT_SOLE_TRADER_LAST_NAME = "Applicant Sole Trader Last Name";
    private static final String RESPONDENT_COMPANY_NAME = "Respondent company name";
    private static final String RESPONDENT_ORGANISATION_NAME = "Respondent organisation name";
    private static final String RESPONDENT_EMAIL_ADDRESS = "respondent@company.com";
    private static final String RESPONDENT_PHONE_NUMBER = "0022002200";
    private static final String RESPONDENT_INDIVIDUAL_FIRST_NAME = "Respondent Individual First Name";
    private static final String RESPONDENT_INDIVIDUAL_LAST_NAME = "Respondent Individual Last Name";
    private static final String RESPONDENT_SOLE_TRADER_FIRST_NAME = "Respondent Sole Trader First Name";
    private static final String RESPONDENT_SOLE_TRADER_LAST_NAME = "Respondent Sole Trader Last Name";
    private static final String TOTAL_AMOUNT = "9000";
    private static final String ID = "123456789";
    private static final String RESPONDENT = "2";
    private static final String APPLICANT = "1";
    private static final String CASE_TITLE = APPLICANT_INDIVIDUAL_FIRST_NAME.concat(" v ").concat(RESPONDENT_INDIVIDUAL_FIRST_NAME);

    private void assertCSVContent(String result, String partyId, String partyName, String partyPhoneNumber, String partyEmailAddress) {
        assertThat(result).contains(ID, partyId, partyName, partyPhoneNumber, partyEmailAddress, TOTAL_AMOUNT, CASE_TITLE);
    }

    private Party buildParty(Party.Type partyType, String companyName, String phoneNumber, String emailAddress,
                             String individualFirstName, String individualLastName,
                             String soleTraderFirstName, String soleTraderLastName, String organisationName) {
        return Party.builder()
            .type(partyType)
            .companyName(companyName)
            .partyPhone(phoneNumber)
            .partyEmail(emailAddress)
            .individualFirstName(individualFirstName)
            .individualLastName(individualLastName)
            .soleTraderFirstName(soleTraderFirstName)
            .soleTraderLastName(soleTraderLastName)
            .organisationName(organisationName)
            .build();
    }

    private CaseData getCaseData(Party.Type partyType) {
        return CaseData.builder()
            .legacyCaseReference(ID)
            .totalClaimAmount(new BigDecimal(9000))
            .applicant1(buildParty(partyType, APPLICANT_COMPANY_NAME, APPLICANT_PHONE_NUMBER, APPLICANT_EMAIL_ADDRESS,
                                   APPLICANT_INDIVIDUAL_FIRST_NAME, APPLICANT_INDIVIDUAL_LAST_NAME,
                                   APPLICANT_SOLE_TRADER_FIRST_NAME, APPLICANT_SOLE_TRADER_LAST_NAME, APPLICANT_ORGANISATION_NAME))
            .respondent1(buildParty(partyType, RESPONDENT_COMPANY_NAME, RESPONDENT_PHONE_NUMBER, RESPONDENT_EMAIL_ADDRESS,
                                    RESPONDENT_INDIVIDUAL_FIRST_NAME, RESPONDENT_INDIVIDUAL_LAST_NAME,
                                    RESPONDENT_SOLE_TRADER_FIRST_NAME, RESPONDENT_SOLE_TRADER_LAST_NAME, RESPONDENT_ORGANISATION_NAME))
            .caseNamePublic(CASE_TITLE)
            .build();
    }

    private void testCSVContent(Party.Type partyType, String applicantName, String respondentName) {
        // Given
        CaseData caseData = getCaseData(partyType);

        // When
        String result = service.generateCSVContent(caseData);

        // Then
        assertCSVContent(result, APPLICANT, applicantName, APPLICANT_PHONE_NUMBER, APPLICANT_EMAIL_ADDRESS);
        assertCSVContent(result, RESPONDENT, respondentName, RESPONDENT_PHONE_NUMBER, RESPONDENT_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturn_properDataForFile_ForIndividual() {
        testCSVContent(Party.Type.INDIVIDUAL,
                       APPLICANT_INDIVIDUAL_FIRST_NAME + " " + APPLICANT_INDIVIDUAL_LAST_NAME,
                       RESPONDENT_INDIVIDUAL_FIRST_NAME + " " + RESPONDENT_INDIVIDUAL_LAST_NAME);
    }

    @Test
    void shouldReturn_properDataForFile_ForCompany() {
        testCSVContent(Party.Type.COMPANY, APPLICANT_COMPANY_NAME, RESPONDENT_COMPANY_NAME);
    }

    @Test
    void shouldReturn_properDataForFile_ForOrganisation() {
        testCSVContent(Party.Type.ORGANISATION, APPLICANT_ORGANISATION_NAME, RESPONDENT_ORGANISATION_NAME);
    }

    @Test
    void shouldReturn_properDataForFile_ForSoleTrader() {
        testCSVContent(Party.Type.SOLE_TRADER,
                       APPLICANT_SOLE_TRADER_FIRST_NAME + " " + APPLICANT_SOLE_TRADER_LAST_NAME,
                       RESPONDENT_SOLE_TRADER_FIRST_NAME + " " + RESPONDENT_SOLE_TRADER_LAST_NAME);
    }

    @Test
    void shouldReturn_properDataForFile_ForBilingualFlag() {
        testCSVContent(Party.Type.SOLE_TRADER,
                       APPLICANT_SOLE_TRADER_FIRST_NAME + " " + APPLICANT_SOLE_TRADER_LAST_NAME,
                       RESPONDENT_SOLE_TRADER_FIRST_NAME + " " + RESPONDENT_SOLE_TRADER_LAST_NAME);
    }
}
