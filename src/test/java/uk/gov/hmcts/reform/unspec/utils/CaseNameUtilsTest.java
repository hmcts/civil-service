package uk.gov.hmcts.reform.unspec.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.Party;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.unspec.utils.CaseNameUtils.toCaseName;

class CaseNameUtilsTest {

    @Test
    public void caseNameNotNull() {
        CaseData caseData = CaseData.builder()
            .claimant(Party.builder()
                          .type(Party.Type.INDIVIDUAL)
                          .individualTitle("Mr.")
                          .individualLastName("Clark")
                          .individualFirstName("Sam")
                          .build())
            .respondent(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualLastName("Richards")
                            .individualFirstName("Alex")
                            .build())
            .build();
        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isNotNull().isEqualTo("Mr. Sam Clark v Mr. Alex Richards");
    }

    @Test
    public void caseName_whenMultiClaimant() {
        CaseData caseData = CaseData.builder()
            .claimant(Party.builder()
                          .type(Party.Type.INDIVIDUAL)
                          .individualTitle("Mr.")
                          .individualLastName("Clark")
                          .individualFirstName("Sam")
                          .build())
            .claimant2(Party.builder()
                           .type(Party.Type.INDIVIDUAL)
                           .individualTitle("Mr.")
                           .individualLastName("Clark")
                           .individualFirstName("White")
                           .build())
            .respondent(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualLastName("Richards")
                            .individualFirstName("Alex")
                            .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("1 Mr. Sam Clark & 2 Mr. White Clark v Mr. Alex Richards");
    }

    @Test
    public void caseName_whenMultiDefendant() {
        CaseData caseData = CaseData.builder()
            .claimant(Party.builder()
                          .type(Party.Type.INDIVIDUAL)
                          .individualTitle("Mr.")
                          .individualLastName("Clark")
                          .individualFirstName("Sam")
                          .build())
            .respondent2(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mr.")
                             .individualLastName("Richards")
                             .individualFirstName("White")
                             .build())
            .respondent(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualLastName("Richards")
                            .individualFirstName("Alex")
                            .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("Mr. Sam Clark v 1 Mr. Alex Richards & 2 Mr. White Richards");
    }

    @Test
    public void caseNameWhenClaimantIsSoleTrader() {
        CaseData caseData = CaseData.builder()
            .claimant(Party.builder()
                          .type(Party.Type.SOLE_TRADER)
                          .soleTraderTitle("Mrs.")
                          .soleTraderLastName("Hammersmith")
                          .soleTraderFirstName("Georgina")
                          .soleTraderTradingAs("EuroStar")
                          .build())
            .respondent(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualLastName("Richards")
                            .individualFirstName("Alex")
                            .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("Mrs. Georgina Hammersmith T/A EuroStar v Mr. Alex Richards");
    }

    @Test
    public void caseNameWhenDefendantIsSoleTrader() {
        CaseData caseData = CaseData.builder()
            .claimant(Party.builder()
                          .type(Party.Type.INDIVIDUAL)
                          .individualTitle("Mr.")
                          .individualLastName("Richards")
                          .individualFirstName("White")
                          .build())
            .respondent(Party.builder()
                            .type(Party.Type.SOLE_TRADER)
                            .soleTraderTitle("Mr.")
                            .soleTraderLastName("Johnson")
                            .soleTraderFirstName("Boris")
                            .soleTraderTradingAs("UberFlip")
                            .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("Mr. White Richards v Mr. Boris Johnson T/A UberFlip");
    }

    @Test
    public void caseNameWhenBothAreSoleTrader() {
        CaseData caseData = CaseData.builder()
            .claimant(Party.builder()
                          .type(Party.Type.SOLE_TRADER)
                          .soleTraderTitle("Mrs.")
                          .soleTraderLastName("Hammersmith")
                          .soleTraderFirstName("Georgina")
                          .soleTraderTradingAs("EuroStar")
                          .build())
            .respondent(Party.builder()
                            .type(Party.Type.SOLE_TRADER)
                            .soleTraderTitle("Mr.")
                            .soleTraderLastName("Johnson")
                            .soleTraderFirstName("Boris")
                            .soleTraderTradingAs("UberFlip")
                            .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("Mrs. Georgina Hammersmith T/A EuroStar v Mr. Boris Johnson T/A UberFlip");
    }

}
