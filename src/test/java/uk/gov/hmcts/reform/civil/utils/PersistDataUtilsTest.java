package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;

class PersistDataUtilsTest {

    @Test
    void shouldCopyAddress() {
        Address expectedAddress = Address.builder()
            .postCode("E11 5BB")
            .build();

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).build())
            .applicant2(Party.builder().partyName("name").type(INDIVIDUAL).build())
            .respondent1(Party.builder().partyName("name").type(INDIVIDUAL).build())
            .respondent2(Party.builder().partyName("name").type(INDIVIDUAL).build())
            .build();

        CaseData oldCaseData = CaseDataBuilder.builder()
            .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(expectedAddress).build())
            .applicant2(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(expectedAddress).build())
            .respondent1(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(expectedAddress).build())
            .respondent2(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(expectedAddress).build())
            .build();

        CaseData results = PersistDataUtils.persistPartyAddress(oldCaseData, caseData);
        assertThat(results.getApplicant1().getPrimaryAddress()).isEqualTo(expectedAddress);
        assertThat(results.getApplicant2().getPrimaryAddress()).isEqualTo(expectedAddress);
        assertThat(results.getRespondent1().getPrimaryAddress()).isEqualTo(expectedAddress);
        assertThat(results.getRespondent2().getPrimaryAddress()).isEqualTo(expectedAddress);
    }
}
