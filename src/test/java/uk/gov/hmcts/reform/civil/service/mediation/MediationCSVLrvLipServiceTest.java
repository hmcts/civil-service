package uk.gov.hmcts.reform.civil.service.mediation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MediationCSVLrvLipServiceTest {

    @InjectMocks
    private MediationCSVLrvLipService service;

    @Mock
    private uk.gov.hmcts.reform.civil.prd.model.Organisation organisation;

    @Mock
    private OrganisationService organisationService;

    private static final String LR_COMPANY_NAME = "Company";
    private static final String LR_COMPANY_EMAIL = "someone@email.com";
    private static final String LR_COMPANY_NUMBER = "123455";
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

    private static final String CASE_TITLE = "Applicant company name".concat(" v ").concat(RESPONDENT_INDIVIDUAL_FIRST_NAME);

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
            .totalClaimAmount(new BigDecimal(TOTAL_AMOUNT))
            .applicant1(buildParty(Party.Type.COMPANY, "Applicant company name", "0011001100", "applicant@company.com", null, null, null, null, null))
            .respondent1(buildParty(partyType, RESPONDENT_COMPANY_NAME, RESPONDENT_PHONE_NUMBER, RESPONDENT_EMAIL_ADDRESS,
                                    RESPONDENT_INDIVIDUAL_FIRST_NAME, RESPONDENT_INDIVIDUAL_LAST_NAME,
                                    RESPONDENT_SOLE_TRADER_FIRST_NAME, RESPONDENT_SOLE_TRADER_LAST_NAME, RESPONDENT_ORGANISATION_NAME))
            .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(new Organisation().setOrganisationID("123")))
            .applicantSolicitor1ClaimStatementOfTruth(new StatementOfTruth().setName(LR_COMPANY_NAME))
            .applicantSolicitor1UserDetails(new IdamUserDetails().setEmail(LR_COMPANY_EMAIL))
            .caseNamePublic(CASE_TITLE)
            .build();
    }

    @BeforeEach
    void setUp() {
        given(organisation.getName()).willReturn(LR_COMPANY_NAME);
        given(organisation.getCompanyNumber()).willReturn(LR_COMPANY_NUMBER);
        given(organisationService.findOrganisationById(anyString())).willReturn(Optional.of(organisation));
    }

    private void testCSVContent(Party.Type partyType, String respondentName) {
        // Given
        CaseData caseData = getCaseData(partyType);

        // When
        String result = service.generateCSVContent(caseData);

        // Then
        assertCSVContent(result, RESPONDENT, respondentName, RESPONDENT_PHONE_NUMBER, RESPONDENT_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturn_properDataForFile_ForCompany() {
        testCSVContent(Party.Type.COMPANY, RESPONDENT_COMPANY_NAME);
    }

    @Test
    void shouldReturn_properDataForFile_ForOrganisation() {
        testCSVContent(Party.Type.ORGANISATION, RESPONDENT_ORGANISATION_NAME);
    }

    @Test
    void shouldReturn_properDataForFile_ForIndividual() {
        testCSVContent(Party.Type.INDIVIDUAL, RESPONDENT_INDIVIDUAL_FIRST_NAME + " " + RESPONDENT_INDIVIDUAL_LAST_NAME);
    }

    @Test
    void shouldReturn_properDataForFile_ForSoleTrader() {
        testCSVContent(Party.Type.SOLE_TRADER, RESPONDENT_SOLE_TRADER_FIRST_NAME + " " + RESPONDENT_SOLE_TRADER_LAST_NAME);
    }

    @Test
    void shouldReturnLrContactDetailsForApplicant() {
        // Given
        CaseData caseData = getCaseData(Party.Type.SOLE_TRADER);

        // When
        String result = service.generateCSVContent(caseData);

        // Then
        assertCSVContent(result, APPLICANT, LR_COMPANY_NAME, LR_COMPANY_NUMBER, LR_COMPANY_EMAIL);
    }
}
