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
    void shouldReturn_properDataForFile() {
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
}
