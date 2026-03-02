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
        Address expectedAddress = new Address();
        expectedAddress.setPostCode("E11 5BB");

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(new Party().setPartyName("name").setType(INDIVIDUAL))
            .applicant2(new Party().setPartyName("name").setType(INDIVIDUAL))
            .respondent1(new Party().setPartyName("name").setType(INDIVIDUAL))
            .respondent2(new Party().setPartyName("name").setType(INDIVIDUAL))
            .build();

        CaseData oldCaseData = CaseDataBuilder.builder()
            .applicant1(new Party().setPartyName("name").setType(INDIVIDUAL).setPrimaryAddress(expectedAddress))
            .applicant2(new Party().setPartyName("name").setType(INDIVIDUAL).setPrimaryAddress(expectedAddress))
            .respondent1(new Party().setPartyName("name").setType(INDIVIDUAL).setPrimaryAddress(expectedAddress))
            .respondent2(new Party().setPartyName("name").setType(INDIVIDUAL).setPrimaryAddress(expectedAddress))
            .build();

        CaseData results = PersistDataUtils.persistPartyAddress(oldCaseData, caseData);
        assertThat(results.getApplicant1().getPrimaryAddress()).isEqualTo(expectedAddress);
        assertThat(results.getApplicant2().getPrimaryAddress()).isEqualTo(expectedAddress);
        assertThat(results.getRespondent1().getPrimaryAddress()).isEqualTo(expectedAddress);
        assertThat(results.getRespondent2().getPrimaryAddress()).isEqualTo(expectedAddress);
    }
}
