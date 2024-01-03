package uk.gov.hmcts.reform.civil.service.mediation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = {
    MediationCSVLipVLipService.class
})
public class MediationCSVLipVLipServiceTest {

    private static final String APPLICANT_COMPANY_NAME = "Applicant company name";
    private static final String APPLICANT_ORGANISATION_NAME = "Applicant organisation name";
    private static final String APPLICANT_EMAIL_ADDRESS = "Applicant@company.com";
    private static final String APPLICANT_PHONE_NUMBER = "7553072111";
    private static final String APPLICANT_INDIVIDUAL_FIST_NAME = "Applicant Individual First Name";
    private static final String APPLICANT_INDIVIDUAL_LAST_NAME = "Applicant Individual Last Name";
    private static final String APPLICANT_INDIVIDUAL_SOLE_TRADER_FIRST_NAME = "Applicant Sole Trader First Name";
    private static final String APPLICANT_INDIVIDUAL_SOLE_TRADER_LAST_NAME = "Applicant Sole Trader Last Name";
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
    private static final String RESPONDENT = "2";
    private static final String APPLICANT = "1";
    private static final String BILINGUAL_FLAG  = "Yes";
    private static final String BILINGUAL_FLAG_NO  = "No";

    @Autowired
    private MediationCSVLipVLipService service;

    @Test
    void shouldReturn_properDataForFile_ForIndividual() {
        //Given
        CaseData caseData = getCaseData(Party.Type.INDIVIDUAL, Language.BOTH.toString());
        //When
        String result = service.generateCSVContent(caseData, true);
        //Then
        assertThat(result).contains(ID);
        assertThat(result).contains(APPLICANT);
        assertThat(result).contains(APPLICANT_INDIVIDUAL_FIST_NAME + " " + APPLICANT_INDIVIDUAL_LAST_NAME);
        assertThat(result).contains(APPLICANT_PHONE_NUMBER);
        assertThat(result).contains(APPLICANT_EMAIL_ADDRESS);
        assertThat(result).contains(RESPONDENT);
        assertThat(result).contains(RESPONDENT_INDIVIDUAL_FIST_NAME + " " + RESPONDENT_INDIVIDUAL_LAST_NAME);
        assertThat(result).contains(TOTAL_AMOUNT);
        assertThat(result).contains(RESPONDENT_PHONE_NUMBER);
        assertThat(result).contains(RESPONDENT_EMAIL_ADDRESS);
        assertThat(result).contains(BILINGUAL_FLAG);
    }

    @Test
    void shouldReturn_properDataForFile_ForCompany() {
        //Given
        CaseData caseData = getCaseData(Party.Type.COMPANY, Language.ENGLISH.toString());
        //When
        String result = service.generateCSVContent(caseData, true);
        //Then
        assertThat(result).contains(ID);
        assertThat(result).contains(APPLICANT);
        assertThat(result).contains(APPLICANT_COMPANY_NAME);
        assertThat(result).contains(APPLICANT_PHONE_NUMBER);
        assertThat(result).contains(APPLICANT_EMAIL_ADDRESS);
        assertThat(result).contains(RESPONDENT);
        assertThat(result).contains(RESPONDENT_COMPANY_NAME);
        assertThat(result).contains(TOTAL_AMOUNT);
        assertThat(result).contains(RESPONDENT_PHONE_NUMBER);
        assertThat(result).contains(RESPONDENT_EMAIL_ADDRESS);
        assertThat(result).contains(BILINGUAL_FLAG_NO);
    }

    @Test
    void shouldReturn_properDataForFile_ForOrganisation() {
        //Given
        CaseData caseData = getCaseData(Party.Type.ORGANISATION,  Language.WELSH.toString());
        //When
        String result = service.generateCSVContent(caseData, true);
        //Then
        assertThat(result).contains(ID);
        assertThat(result).contains(APPLICANT);
        assertThat(result).contains(APPLICANT_ORGANISATION_NAME);
        assertThat(result).contains(APPLICANT_PHONE_NUMBER);
        assertThat(result).contains(APPLICANT_EMAIL_ADDRESS);
        assertThat(result).contains(RESPONDENT);
        assertThat(result).contains(RESPONDENT_ORGANISATION_NAME);
        assertThat(result).contains(TOTAL_AMOUNT);
        assertThat(result).contains(RESPONDENT_PHONE_NUMBER);
        assertThat(result).contains(RESPONDENT_EMAIL_ADDRESS);
        assertThat(result).contains(BILINGUAL_FLAG);
    }

    @Test
    void shouldReturn_properDataForFile_ForSoleTrader() {
        //Given
        CaseData caseData = getCaseData(Party.Type.SOLE_TRADER, Language.ENGLISH.toString());
        //When
        String result = service.generateCSVContent(caseData, true);
        //Then
        assertThat(result).contains(ID);
        assertThat(result).contains(APPLICANT);
        assertThat(result).contains(APPLICANT_INDIVIDUAL_SOLE_TRADER_FIRST_NAME + " " + APPLICANT_INDIVIDUAL_SOLE_TRADER_LAST_NAME);
        assertThat(result).contains(APPLICANT_PHONE_NUMBER);
        assertThat(result).contains(APPLICANT_EMAIL_ADDRESS);
        assertThat(result).contains(RESPONDENT);
        assertThat(result).contains(RESPONDENT_INDIVIDUAL_SOLE_TRADER_FIRST_NAME + " " + RESPONDENT_INDIVIDUAL_SOLE_TRADER_LAST_NAME);
        assertThat(result).contains(TOTAL_AMOUNT);
        assertThat(result).contains(RESPONDENT_PHONE_NUMBER);
        assertThat(result).contains(RESPONDENT_EMAIL_ADDRESS);
        assertThat(result).contains(BILINGUAL_FLAG_NO);
    }

    @Test
    void shouldReturn_properDataForFile_ForBilingualFlag() {
        //Given
        CaseData caseData = getCaseData(Party.Type.SOLE_TRADER, Language.BOTH.toString());
        //When
        String result = service.generateCSVContent(caseData, true);
        //Then
        assertThat(result).contains(ID);
        assertThat(result).contains(APPLICANT);
        assertThat(result).contains(APPLICANT_INDIVIDUAL_SOLE_TRADER_FIRST_NAME + " " + APPLICANT_INDIVIDUAL_SOLE_TRADER_LAST_NAME);
        assertThat(result).contains(APPLICANT_PHONE_NUMBER);
        assertThat(result).contains(APPLICANT_EMAIL_ADDRESS);
        assertThat(result).contains(RESPONDENT);
        assertThat(result).contains(RESPONDENT_INDIVIDUAL_SOLE_TRADER_FIRST_NAME + " " + RESPONDENT_INDIVIDUAL_SOLE_TRADER_LAST_NAME);
        assertThat(result).contains(TOTAL_AMOUNT);
        assertThat(result).contains(RESPONDENT_PHONE_NUMBER);
        assertThat(result).contains(RESPONDENT_EMAIL_ADDRESS);
        assertThat(result).contains(BILINGUAL_FLAG);
    }

    private CaseData getCaseData(Party.Type partyType, String bilingualFlag) {
        CaseData caseData = CaseData.builder()
                .legacyCaseReference(ID)
                .totalClaimAmount(new BigDecimal(9000))
                .claimantBilingualLanguagePreference(bilingualFlag)
                .applicant1(Party.builder()
                        .type(partyType)
                        .companyName(APPLICANT_COMPANY_NAME)
                        .partyPhone(APPLICANT_PHONE_NUMBER)
                        .partyEmail(APPLICANT_EMAIL_ADDRESS)
                        .individualFirstName(APPLICANT_INDIVIDUAL_FIST_NAME)
                        .individualLastName(APPLICANT_INDIVIDUAL_LAST_NAME)
                        .soleTraderFirstName(APPLICANT_INDIVIDUAL_SOLE_TRADER_FIRST_NAME)
                        .soleTraderLastName(APPLICANT_INDIVIDUAL_SOLE_TRADER_LAST_NAME)
                        .organisationName(APPLICANT_ORGANISATION_NAME)
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
                .build();
        return caseData;
    }
}