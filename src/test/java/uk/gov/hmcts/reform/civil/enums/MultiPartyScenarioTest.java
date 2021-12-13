package uk.gov.hmcts.reform.civil.enums;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isMultiPartyScenario;

class MultiPartyScenarioTest {

    @Test
    void shouldReturnOneVOne_WhenOneRespondentAndApplicant() {
        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().build())
            .applicant1(PartyBuilder.builder().build())
            .build();

        assertThat(getMultiPartyScenario(caseData)).isEqualTo(ONE_V_ONE);
    }

    @Test
    void shouldReturnTwoVOne_WhenOneRespondentAndTwoApplicants() {
        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().build())
            .applicant1(PartyBuilder.builder().build())
            .addApplicant2(YesOrNo.YES)
            .applicant2(PartyBuilder.builder().build())
            .build();

        assertThat(getMultiPartyScenario(caseData)).isEqualTo(TWO_V_ONE);
    }

    @Test
    void shouldReturnOneVTwoTwoRepWhenTwoRespondentsRepresentedByDifferentReps() {
        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().build())
            .respondent2(PartyBuilder.builder().build())
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .applicant1(PartyBuilder.builder().build())
            .build();

        assertThat(getMultiPartyScenario(caseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);
    }

    @Test
    void shouldReturnOneVTwoWhenRespondent1IsRepresentedAndRespondent2NotRepresented() {
        // When respondent 2 is not represented there is no screen to for to choose respondent2SameLegalRepresentative,
        // therefore respondent2SameLegalRepresentative is null
        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().build())
            .respondent2(PartyBuilder.builder().build())
            .applicant1(PartyBuilder.builder().build())
            .build();

        assertThat(getMultiPartyScenario(caseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);
    }

    @Test
    void shouldReturnOneVTwoOneRepWhenTwoRespondentsRepresentedBySameReps() {
        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().build())
            .respondent2(PartyBuilder.builder().build())
            .addRespondent2(YesOrNo.YES)
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .applicant1(PartyBuilder.builder().build())
            .build();

        assertThat(getMultiPartyScenario(caseData)).isEqualTo(ONE_V_TWO_ONE_LEGAL_REP);
    }

    @Test
    void shouldReturnTrueWhenMultiPartyScenario() {
        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().build())
            .applicant1(PartyBuilder.builder().build())
            .addApplicant2(YesOrNo.YES)
            .applicant2(PartyBuilder.builder().build())
            .build();
        Assertions.assertTrue(isMultiPartyScenario(caseData));

    }

    @Test
    void shouldReturnFalseWhenNotMultiPartyScenario() {
        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().build())
            .applicant1(PartyBuilder.builder().build())
            .build();
        Assertions.assertFalse(isMultiPartyScenario(caseData));

    }
}
