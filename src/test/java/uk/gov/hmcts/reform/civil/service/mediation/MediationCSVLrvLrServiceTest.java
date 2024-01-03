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
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = {
    MediationCSVLrvLrService.class
})
public class MediationCSVLrvLrServiceTest {

    private static final String LR_APPLICANT_COMPANY_NAME = "AppCompany";
    private static final String LR_APPLICANT_COMPANY_EMAIL = "Appsomeone@email.com";
    private static final String LR_APPLICANT_COMPANY_NUMBER = "123455";
    private static final String LR_DEFENDANT_COMPANY_NAME = "DefCompany";
    private static final String LR_DEFENDANT_COMPANY_EMAIL = "Defsomeone@email.com";
    private static final String LR_DEFENDANT_COMPANY_NUMBER = "123455";
    private static final String APPLICANT_COMPANY_NAME = "Applicant company name";
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
    private static final String APP_LR_ORG_ID = "123";
    private static final String DEF_LR_ORG_ID = "456";
    private static final boolean r2FlagEnabled = false;

    @Mock
    private uk.gov.hmcts.reform.civil.prd.model.Organisation applicantOrganisation;

    @Mock
    private uk.gov.hmcts.reform.civil.prd.model.Organisation defendantOrganisation;

    @MockBean
    private OrganisationService organisationService;

    @Autowired
    private MediationCSVLrvLrService service;

    @BeforeEach
    void setUp() {
        given(applicantOrganisation.getName()).willReturn(LR_APPLICANT_COMPANY_NAME);
        given(applicantOrganisation.getCompanyNumber()).willReturn(LR_APPLICANT_COMPANY_NUMBER);
        given(defendantOrganisation.getName()).willReturn(LR_DEFENDANT_COMPANY_NAME);
        given(defendantOrganisation.getCompanyNumber()).willReturn(LR_DEFENDANT_COMPANY_NUMBER);
        given(organisationService.findOrganisationById(APP_LR_ORG_ID)).willReturn(Optional.ofNullable(
            applicantOrganisation));
        given(organisationService.findOrganisationById(DEF_LR_ORG_ID)).willReturn(Optional.ofNullable(
            defendantOrganisation));
    }

    @Test
    void shouldContainApplicantLRContactDetails() {
        //Given
        CaseData caseData = getCaseData(Party.Type.COMPANY);
        //When
        String result = service.generateCSVContent(caseData, r2FlagEnabled);
        //Then
        assertThat(result).contains(LR_APPLICANT_COMPANY_NAME);
        assertThat(result).contains(LR_APPLICANT_COMPANY_EMAIL);
        assertThat(result).contains(LR_APPLICANT_COMPANY_NUMBER);
    }

    @Test
    void shouldContainRespondentLRContactDetails() {
        //Given
        CaseData caseData = getCaseData(Party.Type.COMPANY);
        //When
        String result = service.generateCSVContent(caseData, r2FlagEnabled);
        //Then
        assertThat(result).contains(LR_DEFENDANT_COMPANY_NAME);
        assertThat(result).contains(LR_DEFENDANT_COMPANY_EMAIL);
        assertThat(result).contains(LR_DEFENDANT_COMPANY_NUMBER);
    }

    @Test
    void shouldContainApplicantCompanyName_whenTypeIsCompany() {
        //Given
        CaseData caseData = getCaseData(Party.Type.COMPANY);
        //When
        String result = service.generateCSVContent(caseData, r2FlagEnabled);
        //Then
        assertThat(result).contains(APPLICANT_COMPANY_NAME);
    }

    @Test
    void shouldContainDefendantCompanyName_whenTypeIsCompany() {
        //Given
        CaseData caseData = getCaseData(Party.Type.COMPANY);
        //When
        String result = service.generateCSVContent(caseData, r2FlagEnabled);
        //Then
        assertThat(result).contains(RESPONDENT_COMPANY_NAME);
    }

    @Test
    void shouldContainTotalAmount() {
        //Given
        CaseData caseData = getCaseData(Party.Type.COMPANY);
        //When
        String result = service.generateCSVContent(caseData, r2FlagEnabled);
        //Then
        assertThat(result).contains(TOTAL_AMOUNT);
    }

    private CaseData getCaseData(Party.Type partyType) {
        CaseData caseData = CaseData.builder()
            .legacyCaseReference(ID)
            .totalClaimAmount(new BigDecimal(9000))
            .applicant1(Party.builder()
                            .type(partyType)
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
                                                                .organisationID(APP_LR_ORG_ID)
                                                                .build())
                                              .build())
            .respondent1OrganisationPolicy(OrganisationPolicy
                                               .builder()
                                               .organisation(Organisation
                                                                 .builder()
                                                                 .organisationID(DEF_LR_ORG_ID)
                                                                 .build())
                                               .build())
            .applicantSolicitor1ClaimStatementOfTruth(
                StatementOfTruth.builder()
                    .name(LR_APPLICANT_COMPANY_NAME)
                    .build()
            )
            .applicantSolicitor1UserDetails(
                IdamUserDetails.builder()
                    .email(LR_APPLICANT_COMPANY_EMAIL)
                    .build()
            )
            .respondentSolicitor1EmailAddress(LR_DEFENDANT_COMPANY_EMAIL)
            .build();
        return caseData;
    }
}
