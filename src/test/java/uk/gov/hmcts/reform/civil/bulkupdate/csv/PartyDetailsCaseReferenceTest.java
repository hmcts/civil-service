package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.Party;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class PartyDetailsCaseReferenceTest {

    private PartyDetailsCaseReference caseReference;

    @BeforeEach
    void setUp() {
        caseReference = new PartyDetailsCaseReference();
    }

    @Test
    void shouldSetCaseReferenceFromRowValues() throws Exception {
        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("caseReference", "CASE-123");

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getCaseReference()).isEqualTo("CASE-123");
    }

    @Test
    void shouldMapApplicant1PartyCorrectly() throws Exception {
        Party party = Party.builder()
            .individualFirstName("Alice")
            .individualLastName("Smith")
            .type(Party.Type.INDIVIDUAL)
            .build();

        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("applicant1", party);

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getDataObject()).isEqualTo(party);
        assertThat(caseReference.getParty()).isEqualTo(party);
        assertThat(caseReference.isApplicant1()).isTrue();
        assertThat(caseReference.isApplicant2()).isFalse();
        assertThat(caseReference.isRespondent1()).isFalse();
        assertThat(caseReference.isRespondent2()).isFalse();
    }

    @Test
    void shouldMapApplicant2PartyCorrectly() throws Exception {
        Party party = Party.builder()
            .individualFirstName("Bob")
            .individualLastName("Johnson")
            .type(Party.Type.INDIVIDUAL)
            .build();

        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("applicant2", party);

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getDataObject()).isEqualTo(party);
        assertThat(caseReference.getParty()).isEqualTo(party);
        assertThat(caseReference.isApplicant2()).isTrue();
        assertThat(caseReference.isApplicant1()).isFalse();
        assertThat(caseReference.isRespondent1()).isFalse();
        assertThat(caseReference.isRespondent2()).isFalse();
    }

    @Test
    void shouldMapRespondent1PartyCorrectly() throws Exception {
        Party party = Party.builder()
            .individualFirstName("Charlie")
            .individualLastName("Brown")
            .type(Party.Type.INDIVIDUAL)
            .build();

        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("respondent1", party);

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getDataObject()).isEqualTo(party);
        assertThat(caseReference.getParty()).isEqualTo(party);
        assertThat(caseReference.isRespondent1()).isTrue();
        assertThat(caseReference.isApplicant1()).isFalse();
        assertThat(caseReference.isApplicant2()).isFalse();
        assertThat(caseReference.isRespondent2()).isFalse();
        assertThat(caseReference.getParty()).isEqualTo(party);
    }

    @Test
    void shouldMapRespondent2PartyCorrectly() throws Exception {
        Party party = Party.builder()
            .individualFirstName("Diana")
            .individualLastName("Prince")
            .type(Party.Type.INDIVIDUAL)
            .build();

        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("respondent2", party);

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getDataObject()).isEqualTo(party);
        assertThat(caseReference.getParty()).isEqualTo(party);
        assertThat(caseReference.isRespondent2()).isTrue();
        assertThat(caseReference.isApplicant1()).isFalse();
        assertThat(caseReference.isApplicant2()).isFalse();
        assertThat(caseReference.isRespondent1()).isFalse();
    }

    @Test
    void shouldIgnoreUnknownKeysGracefully() throws Exception {
        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("unknownKey", "irrelevant");
        rowValues.put("caseReference", "CASE-999");

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getCaseReference()).isEqualTo("CASE-999");
        assertThat(caseReference.getDataObject()).isNull();
        assertThat(caseReference.getParty()).isNull();
        assertThat(caseReference.isApplicant1()).isFalse();
        assertThat(caseReference.isApplicant2()).isFalse();
        assertThat(caseReference.isRespondent1()).isFalse();
        assertThat(caseReference.isRespondent2()).isFalse();
    }
}
