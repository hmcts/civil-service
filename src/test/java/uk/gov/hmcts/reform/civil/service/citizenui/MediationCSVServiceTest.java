package uk.gov.hmcts.reform.civil.service.citizenui;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = {
    MediationCSVService.class
})
public class MediationCSVServiceTest {

    @Autowired
    MediationCSVService service;

    @Test
    void shouldReturn_properDataForFile_ForCompany() {
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("123456789")
            .totalClaimAmount(new BigDecimal(9000))
            .applicant1(Party.builder()
                            .type(Party.Type.COMPANY)
                            .companyName("Applicant company name")
                            .partyPhone("0011001100")
                            .partyEmail("applicant@company.com")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("Respondent company name")
                             .partyPhone("0022002200")
                             .partyEmail("respondent@company.com")
                             .build())
            .build();

        String result = service.generateCSVContent(caseData);

        assertThat(result).contains("123456789");
        assertThat(result).contains("COMPANY");
        assertThat(result).contains("Applicant company name");
        assertThat(result).contains("9000");
        assertThat(result).contains("0011001100");
        assertThat(result).contains("applicant@company.com");
    }

    @Test
    void shouldReturn_properDataForFile_ForOrganisation() {
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("123456789")
            .totalClaimAmount(new BigDecimal(9000))
            .applicant1(Party.builder()
                            .type(Party.Type.ORGANISATION)
                            .organisationName("Applicant organisation name")
                            .partyPhone("0011001100")
                            .partyEmail("applicant@organisation.com")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.ORGANISATION)
                             .companyName("Respondent organisation name")
                             .partyPhone("0022002200")
                             .partyEmail("respondent@organisation.com")
                             .build())
            .build();

        String result = service.generateCSVContent(caseData);

        assertThat(result).contains("123456789");
        assertThat(result).contains("ORGANISATION");
        assertThat(result).contains("Applicant organisation name");
        assertThat(result).contains("9000");
        assertThat(result).contains("0011001100");
        assertThat(result).contains("applicant@organisation.com");
    }

    @Test
    void shouldReturn_properDataForFile_ForIndividual() {
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("123456789")
            .totalClaimAmount(new BigDecimal(9000))
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualFirstName("Applicant Individual First Name")
                            .individualLastName("Applicant Individual Last Name")
                            .companyName("Applicant company name")
                            .partyPhone("0011001100")
                            .partyEmail("applicant@individual.com")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualFirstName("Respondent Individual First Name")
                             .individualLastName("Respondent Individual Last Name")
                             .partyPhone("0022002200")
                             .partyEmail("respondent@individual.com")
                             .build())
            .build();

        String result = service.generateCSVContent(caseData);

        assertThat(result).contains("123456789");
        assertThat(result).contains("COMPANY");
        assertThat(result).contains("Applicant Individual First Name Applicant Individual Last Name");
        assertThat(result).contains("9000");
        assertThat(result).contains("0011001100");
        assertThat(result).contains("applicant@individual.com");
    }

    @Test
    void shouldReturn_properDataForFile_ForSoleTrader() {
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("123456789")
            .totalClaimAmount(new BigDecimal(9000))
            .applicant1(Party.builder()
                            .type(Party.Type.SOLE_TRADER)
                            .soleTraderFirstName("Applicant Sole Trader First Name")
                            .soleTraderLastName("Applicant Sole Trader Last Name")
                            .partyPhone("0011001100")
                            .partyEmail("applicant@soletrader.com")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.SOLE_TRADER)
                             .soleTraderFirstName("Respondent Sole Trader First Name")
                             .soleTraderLastName("Respondent Sole Trader Last Name")
                             .partyPhone("0011001100")
                             .partyPhone("0022002200")
                             .partyEmail("respondent@soletrader.com")
                             .build())
            .build();

        String result = service.generateCSVContent(caseData);

        assertThat(result).contains("123456789");
        assertThat(result).contains("SOLE_TRADER");
        assertThat(result).contains("Applicant Sole Trader First Name Applicant Sole Trader Last Name");
        assertThat(result).contains("9000");
        assertThat(result).contains("0011001100");
        assertThat(result).contains("applicant@soletrader.com");
    }
}

