package uk.gov.hmcts.reform.civil.service.mediation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = {
    MediationCSVLrvLipService.class
})
public class MediationCSVLrvLipServiceTest {

    private static final String LR_COMPANY_NAME = "Company";
    private static final String LR_COMPANY_EMAIL = "someone@email.com";
    private static final String LR_COMPANY_NUMBER = "123455";
    private static final String RESPONDENT_COMPANY_NAME = "Respondent company name";
    private static final String RESPONDENT_ORGANISATION_NAME = "Respondent organisation name";
    private static final String RESPONDENT_EMAIL_ADDRESS = "respondent@company.com";
    private static final String RESPONDENT_PHONE_NUMBER = "0022002200";
    private static final String RESPONDENT_INDIVIDUAL_FIST_NAME = "Respondent Individual First Name";
    private static final String RESPONDENT_INDIVIDUAL_LAST_NAME = "Respondent Individual Last Name";
    private static final String RESPONDENT_INDIVIDUAL_SOLE_TRADER_FIRST_NAME = "Respondent Sole Trader First Name";
    private static final String RESPONDENT_INDIVIDUAL_SOLE_TRADER_LAST_NAME = "Respondent Sole Trader Last Name";
    private static final String TOTAL_AMOUNT = "9000";
    private static final String ID = "123456789";

    @Mock
    private uk.gov.hmcts.reform.civil.prd.model.Organisation organisation;

    @MockBean
    private OrganisationService organisationService;

    @Autowired
    private MediationCSVLrvLipService service;

    @BeforeEach
    void setUp() {
        given(organisation.getName()).willReturn(LR_COMPANY_NAME);
        given(organisation.getCompanyNumber()).willReturn(LR_COMPANY_NUMBER);
        given(organisationService.findOrganisationById(anyString())).willReturn(Optional.ofNullable(organisation));
    }

    @Test
    void shouldReturn_properDataForFile_ForCompany() {
        //Given
        CaseData caseData = getCaseData(Party.Type.COMPANY);
        //When
        String result = service.generateCSVContent(caseData);
        //Then
        assertThat(result).contains(ID);
        assertThat(result).contains(Party.Type.COMPANY.name());
        assertThat(result).contains(RESPONDENT_COMPANY_NAME);
        assertThat(result).contains(TOTAL_AMOUNT);
        assertThat(result).contains(RESPONDENT_PHONE_NUMBER);
        assertThat(result).contains(RESPONDENT_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturn_properDataForFile_ForOrganisation() {
        //Given
        CaseData caseData = getCaseData(Party.Type.ORGANISATION);
        //When
        String result = service.generateCSVContent(caseData);
        //Then
        assertThat(result).contains(ID);
        assertThat(result).contains("ORGANISATION");
        assertThat(result).contains(RESPONDENT_ORGANISATION_NAME);
        assertThat(result).contains(TOTAL_AMOUNT);
        assertThat(result).contains(RESPONDENT_PHONE_NUMBER);
        assertThat(result).contains(RESPONDENT_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturn_properDataForFile_ForIndividual() {
        //Given
        CaseData caseData = getCaseData(Party.Type.INDIVIDUAL);
        //When
        String result = service.generateCSVContent(caseData);
        //Then
        assertThat(result).contains(ID);
        assertThat(result).contains(Party.Type.INDIVIDUAL.name());
        assertThat(result).contains(RESPONDENT_INDIVIDUAL_FIST_NAME + " " + RESPONDENT_INDIVIDUAL_LAST_NAME);
        assertThat(result).contains(TOTAL_AMOUNT);
        assertThat(result).contains(RESPONDENT_PHONE_NUMBER);
        assertThat(result).contains(RESPONDENT_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturn_properDataForFile_ForSoleTrader() {
        //Given
        CaseData caseData = getCaseData(Party.Type.SOLE_TRADER);
        //When
        String result = service.generateCSVContent(caseData);
        //Then
        assertThat(result).contains(ID);
        assertThat(result).contains("SOLE_TRADER");
        assertThat(result).contains(RESPONDENT_INDIVIDUAL_SOLE_TRADER_FIRST_NAME + " " + RESPONDENT_INDIVIDUAL_SOLE_TRADER_LAST_NAME);
        assertThat(result).contains(TOTAL_AMOUNT);
        assertThat(result).contains(RESPONDENT_PHONE_NUMBER);
        assertThat(result).contains(RESPONDENT_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnLrContactDetailsForApplicant() {
        //Given
        CaseData caseData = getCaseData(Party.Type.SOLE_TRADER);
        //When
        String result = service.generateCSVContent(caseData);
        //Then
        assertThat(result).contains(LR_COMPANY_NAME);
        assertThat(result).contains(LR_COMPANY_EMAIL);
        assertThat(result).contains(LR_COMPANY_NUMBER);
    }

    private CaseData getCaseData(Party.Type partyType) {
        CaseData caseData = CaseData.builder()
            .legacyCaseReference(ID)
            .totalClaimAmount(new BigDecimal(9000))
            .applicant1(Party.builder()
                            .type(Party.Type.COMPANY)
                            .companyName("Applicant company name")
                            .partyPhone("0011001100")
                            .partyEmail("applicant@company.com")
                            .build())
            .respondent1(Party.builder()
                             .type(partyType)
                             .soleTraderFirstName(RESPONDENT_INDIVIDUAL_SOLE_TRADER_FIRST_NAME)
                             .soleTraderLastName(RESPONDENT_INDIVIDUAL_SOLE_TRADER_LAST_NAME)
                             .individualFirstName(RESPONDENT_INDIVIDUAL_FIST_NAME)
                             .individualLastName(RESPONDENT_INDIVIDUAL_LAST_NAME)
                             .companyName(RESPONDENT_COMPANY_NAME)
                             .organisationName(RESPONDENT_ORGANISATION_NAME)
                             .partyPhone(RESPONDENT_PHONE_NUMBER)
                             .partyEmail(RESPONDENT_EMAIL_ADDRESS)
                             .build())
            .applicant1OrganisationPolicy(OrganisationPolicy
                                              .builder()
                                              .organisation(Organisation
                                                                .builder()
                                                                .organisationID("123")
                                                                .build())
                                              .build())
            .applicantSolicitor1ClaimStatementOfTruth(
                StatementOfTruth.builder()
                    .name(LR_COMPANY_NAME)
                    .build()
            )
            .applicantSolicitor1UserDetails(
                IdamUserDetails.builder()
                    .email(LR_COMPANY_EMAIL)
                    .build()
            )
            .build();
        return caseData;
    }
}

