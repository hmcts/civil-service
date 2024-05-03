package uk.gov.hmcts.reform.civil.enums;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVOne;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoLegalRep;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isTwoVOne;

class MultiPartyScenarioTest {

    @Test
    void shouldReturnOneVOne_WhenOneRespondentAndApplicant() {
        CaseData caseData = get1V1CaseData();

        assertThat(getMultiPartyScenario(caseData)).isEqualTo(ONE_V_ONE);
    }

    @Test
    void shouldReturnTwoVOne_WhenOneRespondentAndTwoApplicants() {
        CaseData caseData = get2V1CaseData();
        assertThat(getMultiPartyScenario(caseData)).isEqualTo(TWO_V_ONE);
    }

    private static CaseData get2V1CaseData() {
        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().build())
            .applicant1(PartyBuilder.builder().build())
            .addApplicant2(YesOrNo.YES)
            .applicant2(PartyBuilder.builder().build())
            .build();
        return caseData;
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
        CaseData caseData = getOneVTwoTwoLegalRepCaseData();

        assertThat(getMultiPartyScenario(caseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);
    }

    private static CaseData getOneVTwoTwoLegalRepCaseData() {
        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().build())
            .respondent2(PartyBuilder.builder().build())
            .applicant1(PartyBuilder.builder().build())
            .build();
        return caseData;
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
        CaseData caseData = get2V1CaseData();
        assertTrue(isMultiPartyScenario(caseData));

    }

    @Test
    void shouldReturnFalseWhenNotMultiPartyScenario() {
        CaseData caseData = get1V1CaseData();
        assertFalse(isMultiPartyScenario(caseData));

    }

    @Test
    void shouldReturnTrueWhenOneToOne() {
        CaseData caseData = get1V1CaseData();
        assertTrue(isOneVOne(caseData));
    }

    private static CaseData get1V1CaseData() {
        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().build())
            .applicant1(PartyBuilder.builder().build())
            .build();
        return caseData;
    }

    @Test
    void shouldReturnFalseWhenNotOneVOne() {
        CaseData caseData = get1V2CaseData();
        assertFalse(isOneVOne(caseData));
    }

    @Test
    void shouldReturnTrueWhenOneVTwo() {
        CaseData caseData = get1V2CaseData();
        assertTrue(isOneVTwoLegalRep(caseData));
    }

    @Test
    void shouldReturnFalseWhenNotOneVTwo() {
        CaseData caseData = get1V1CaseData();
        assertFalse(isOneVTwoLegalRep(caseData));
    }

    @Test
    void shouldReturnTrueWhenTwoVOne() {
        CaseData caseData = get2V1CaseData();
        assertTrue(isTwoVOne(caseData));
    }

    @Test
    void shouldReturnFalseWhenNotTwoVOne() {
        CaseData caseData = get1V1CaseData();
        assertFalse(isTwoVOne(caseData));
    }

    @Test
    void shouldReturnTrueWhenOneVTwoTwoLegalRep() {
        CaseData caseData = getOneVTwoTwoLegalRepCaseData();
        assertTrue(isOneVTwoTwoLegalRep(caseData));
    }

    @Test
    void shouldReturnFalseWhenNotOneVTwoTwoLegalRep() {
        CaseData caseData = get1V1CaseData();
        assertFalse(isOneVTwoTwoLegalRep(caseData));
    }

    private static CaseData get1V2CaseData() {
        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().build())
            .respondent2(PartyBuilder.builder().build())
            .addRespondent2(YesOrNo.YES)
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .applicant1(PartyBuilder.builder().build())
            .build();
        return caseData;
    }
}
