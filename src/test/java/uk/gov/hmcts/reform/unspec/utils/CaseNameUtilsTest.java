package uk.gov.hmcts.reform.unspec.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.Party;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.unspec.utils.CaseNameUtils.toCaseName;

class CaseNameUtilsTest {

    @Test
    void shouldReturnCaseName_whenBothPartiesAreIndividuals() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .partyName("Mr. Sam Clark")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .partyName("Mr. Alex Richards")
                             .build())
            .build();
        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isNotNull().isEqualTo("Mr. Sam Clark v Mr. Alex Richards");
    }

    @Test
    void shouldReturnCaseName_whenMultiClaimant() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .partyName("Mr. Sam Clark")
                            .build())
            .applicant2(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .partyName("Mr. White Clark")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .partyName("Mr. Alex Richards")
                             .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("1 Mr. Sam Clark & 2 Mr. White Clark v Mr. Alex Richards");
    }

    @Test
    void shouldReturnCaseName_whenMultiDefendant() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .partyName("Mr. Sam Clark")
                            .build())
            .respondent2(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .partyName("Mr. White Richards")
                             .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .partyName("Mr. Alex Richards")
                             .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("Mr. Sam Clark v 1 Mr. Alex Richards & 2 Mr. White Richards");
    }

    @Test
    void shouldReturnCaseName_whenClaimantIsSoleTrader() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.SOLE_TRADER)
                            .partyName("Mrs. Georgina Hammersmith")
                            .soleTraderTradingAs("EuroStar")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .partyName("Mr. Alex Richards")
                             .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("Mrs. Georgina Hammersmith T/A EuroStar v Mr. Alex Richards");
    }

    @Test
    void shouldReturnCaseName_whenDefendantIsSoleTrader() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .partyName("Mr. White Richards")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.SOLE_TRADER)
                             .partyName("Mr. Boris Johnson")
                             .soleTraderTradingAs("UberFlip")
                             .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("Mr. White Richards v Mr. Boris Johnson T/A UberFlip");
    }

    @Test
    void shouldReturnCaseName_whenBothAreSoleTrader() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.SOLE_TRADER)
                            .partyName("Mrs. Georgina Hammersmith")
                            .soleTraderTradingAs("EuroStar")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.SOLE_TRADER)
                             .partyName("Mr. Boris Johnson")
                             .soleTraderTradingAs("UberFlip")
                             .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("Mrs. Georgina Hammersmith T/A EuroStar v Mr. Boris Johnson T/A UberFlip");
    }
}
