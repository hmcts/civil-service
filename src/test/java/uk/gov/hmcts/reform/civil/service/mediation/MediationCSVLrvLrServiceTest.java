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
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MediationCSVLrvLrServiceTest {

    @InjectMocks
    private MediationCSVLrvLrService service;

    @Mock
    private uk.gov.hmcts.reform.civil.prd.model.Organisation applicantOrganisation;

    @Mock
    private uk.gov.hmcts.reform.civil.prd.model.Organisation defendantOrganisation;

    @Mock
    private OrganisationService organisationService;

    private static final String LR_APPLICANT_COMPANY_NAME = "AppCompany";
    private static final String LR_APPLICANT_COMPANY_EMAIL = "Appsomeone@email.com";
    private static final String LR_APPLICANT_COMPANY_NUMBER = "123455";
    private static final String LR_DEFENDANT_COMPANY_NAME = "DefCompany";
    private static final String LR_DEFENDANT_COMPANY_EMAIL = "Defsomeone@email.com";
    private static final String LR_DEFENDANT_COMPANY_NUMBER = "123455";
    private static final String APPLICANT_COMPANY_NAME = "Applicant company name";
    private static final String RESPONDENT_COMPANY_NAME = "Respondent company name";
    private static final String RESPONDENT_EMAIL_ADDRESS = "respondent@company.com";
    private static final String RESPONDENT_PHONE_NUMBER = "0022002200";
    private static final String RESPONDENT_INDIVIDUAL_FIRST_NAME = "Respondent Individual First Name";
    private static final String RESPONDENT_INDIVIDUAL_LAST_NAME = "Respondent Individual Last Name";
    private static final String RESPONDENT_SOLE_TRADER_FIRST_NAME = "Respondent Sole Trader First Name";
    private static final String RESPONDENT_SOLE_TRADER_LAST_NAME = "Respondent Sole Trader Last Name";
    private static final String TOTAL_AMOUNT = "9000";
    private static final String ID = "123456789";
    private static final String APP_LR_ORG_ID = "123";
    private static final String DEF_LR_ORG_ID = "456";

    @BeforeEach
    void setUp() {
        given(applicantOrganisation.getName()).willReturn(LR_APPLICANT_COMPANY_NAME);
        given(applicantOrganisation.getCompanyNumber()).willReturn(LR_APPLICANT_COMPANY_NUMBER);
        given(defendantOrganisation.getName()).willReturn(LR_DEFENDANT_COMPANY_NAME);
        given(defendantOrganisation.getCompanyNumber()).willReturn(LR_DEFENDANT_COMPANY_NUMBER);
        given(organisationService.findOrganisationById(APP_LR_ORG_ID)).willReturn(Optional.of(applicantOrganisation));
        given(organisationService.findOrganisationById(DEF_LR_ORG_ID)).willReturn(Optional.of(defendantOrganisation));
    }

    private Party buildParty(Party.Type type, String firstName, String lastName, String phoneNumber, String emailAddress) {
        return Party.builder()
            .type(type)
            .individualFirstName(firstName)
            .individualLastName(lastName)
            .partyPhone(phoneNumber)
            .partyEmail(emailAddress)
            .build();
    }

    private Party buildCompanyParty(String companyName, String phoneNumber, String emailAddress) {
        return Party.builder()
            .type(Party.Type.COMPANY)
            .companyName(companyName)
            .partyPhone(phoneNumber)
            .partyEmail(emailAddress)
            .build();
    }

    private CaseData getCaseData(Party.Type partyType) {
        Party applicantParty = buildCompanyParty(APPLICANT_COMPANY_NAME, "0011001100", "applicant@company.com");
        Party respondentParty = switch (partyType) {
            case COMPANY -> buildCompanyParty(RESPONDENT_COMPANY_NAME, RESPONDENT_PHONE_NUMBER, RESPONDENT_EMAIL_ADDRESS);
            case INDIVIDUAL -> buildParty(partyType, RESPONDENT_INDIVIDUAL_FIRST_NAME, RESPONDENT_INDIVIDUAL_LAST_NAME, RESPONDENT_PHONE_NUMBER, RESPONDENT_EMAIL_ADDRESS);
            case SOLE_TRADER -> buildParty(partyType, RESPONDENT_SOLE_TRADER_FIRST_NAME, RESPONDENT_SOLE_TRADER_LAST_NAME, RESPONDENT_PHONE_NUMBER, RESPONDENT_EMAIL_ADDRESS);
            default -> throw new IllegalArgumentException("Invalid party type");
        };

        return CaseData.builder()
            .legacyCaseReference(ID)
            .totalClaimAmount(new BigDecimal(TOTAL_AMOUNT))
            .applicant1(applicantParty)
            .respondent1(respondentParty)
            .caseNamePublic(applicantParty.getPartyName().concat(" v ").concat(respondentParty.getPartyName()))
            .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(new Organisation().setOrganisationID(APP_LR_ORG_ID)))
            .respondent1OrganisationPolicy(new OrganisationPolicy().setOrganisation(new Organisation().setOrganisationID(DEF_LR_ORG_ID)))
            .applicantSolicitor1ClaimStatementOfTruth(new StatementOfTruth().setName(LR_APPLICANT_COMPANY_NAME))
            .applicantSolicitor1UserDetails(new IdamUserDetails().setEmail(LR_APPLICANT_COMPANY_EMAIL))
            .respondentSolicitor1EmailAddress(LR_DEFENDANT_COMPANY_EMAIL)
            .build();
    }

    private void assertContainsDetails(String result, String... details) {
        for (String detail : details) {
            assertThat(result).contains(detail);
        }
    }

    @Test
    void shouldContainApplicantLRContactDetails() {
        // Given
        CaseData caseData = getCaseData(Party.Type.COMPANY);

        // When
        String result = service.generateCSVContent(caseData);

        // Then
        assertContainsDetails(result, LR_APPLICANT_COMPANY_NAME,
                              LR_APPLICANT_COMPANY_EMAIL,
                              LR_APPLICANT_COMPANY_NUMBER,
                              APPLICANT_COMPANY_NAME.concat(" v ").concat(RESPONDENT_COMPANY_NAME));
    }

    @Test
    void shouldContainRespondentLRContactDetails() {
        // Given
        CaseData caseData = getCaseData(Party.Type.COMPANY);

        // When
        String result = service.generateCSVContent(caseData);

        // Then
        assertContainsDetails(result, LR_DEFENDANT_COMPANY_NAME, LR_DEFENDANT_COMPANY_EMAIL, LR_DEFENDANT_COMPANY_NUMBER);
    }

    @Test
    void shouldContainApplicantCompanyName_whenTypeIsCompany() {
        // Given
        CaseData caseData = getCaseData(Party.Type.COMPANY);

        // When
        String result = service.generateCSVContent(caseData);

        // Then
        assertContainsDetails(result, APPLICANT_COMPANY_NAME);
    }

    @Test
    void shouldContainDefendantCompanyName_whenTypeIsCompany() {
        // Given
        CaseData caseData = getCaseData(Party.Type.COMPANY);

        // When
        String result = service.generateCSVContent(caseData);

        // Then
        assertContainsDetails(result, RESPONDENT_COMPANY_NAME);
    }

    @Test
    void shouldContainTotalAmount() {
        // Given
        CaseData caseData = getCaseData(Party.Type.COMPANY);

        // When
        String result = service.generateCSVContent(caseData);

        // Then
        assertContainsDetails(result, TOTAL_AMOUNT);
    }
}
