package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdatePartyWitnessTaskTest {

    private UpdatePartyWitnessTask task;

    @BeforeEach
    void setUp() {
        task = new UpdatePartyWitnessTask();
    }

    @Test
    void shouldThrowExceptionWhenCaseReferenceIsNull() {
        CaseData caseData = CaseData.builder().build();
        assertThrows(IllegalArgumentException.class, () -> task.migrateCaseData(caseData, null));
    }

    @Test
    void shouldThrowExceptionWhenCaseReferenceValueIsNull() {
        CaseData caseData = CaseData.builder().build();
        CaseReference caseRef = CaseReference.builder().caseReference(null).build();
        assertThrows(IllegalArgumentException.class, () -> task.migrateCaseData(caseData, caseRef));
    }

    @Test
    void shouldUpdateApplicantWitnessesWithTBCWhenNamesAreNull() {
        PartyFlagStructure witness1 = PartyFlagStructure.builder().firstName(null).lastName(null).build();
        PartyFlagStructure witness2 = PartyFlagStructure.builder().firstName("Alice").lastName(null).build();

        CaseData caseData = CaseData.builder()
            .applicantWitnesses(List.of(
                Element.<PartyFlagStructure>builder().value(witness1).build(),
                Element.<PartyFlagStructure>builder().value(witness2).build()
            ))
            .build();

        CaseReference ref = CaseReference.builder().caseReference("123").build();

        CaseData updated = task.migrateCaseData(caseData, ref);
        List<Element<PartyFlagStructure>> updatedList = updated.getApplicantWitnesses();

        assertThat(updatedList).hasSize(2);
        assertThat(updatedList.get(0).getValue().getFirstName()).isEqualTo("TBC");
        assertThat(updatedList.get(0).getValue().getLastName()).isEqualTo("TBC");
        assertThat(updatedList.get(1).getValue().getFirstName()).isEqualTo("Alice");
        assertThat(updatedList.get(1).getValue().getLastName()).isEqualTo("TBC");
    }

    @Test
    void shouldUpdateRespondent1WitnessesWithTBCWhenNamesAreNull() {
        PartyFlagStructure witness = PartyFlagStructure.builder().firstName(null).lastName("Smith").build();

        CaseData caseData = CaseData.builder()
            .respondent1Witnesses(List.of(Element.<PartyFlagStructure>builder().value(witness).build()))
            .build();

        CaseReference ref = CaseReference.builder().caseReference("12345").build();
        CaseData updated = task.migrateCaseData(caseData, ref);

        PartyFlagStructure updatedWitness = updated.getRespondent1Witnesses().get(0).getValue();
        assertThat(updatedWitness.getFirstName()).isEqualTo("TBC");
        assertThat(updatedWitness.getLastName()).isEqualTo("Smith");
    }

    @Test
    void shouldUpdateRespondent2WitnessesWithTBCWhenNamesAreNull() {
        PartyFlagStructure witness = PartyFlagStructure.builder().firstName("Bob").lastName(null).build();

        CaseData caseData = CaseData.builder()
            .respondent2Witnesses(List.of(Element.<PartyFlagStructure>builder().value(witness).build()))
            .build();

        CaseReference ref = CaseReference.builder().caseReference("12345").build();
        CaseData updated = task.migrateCaseData(caseData, ref);

        PartyFlagStructure updatedWitness = updated.getRespondent2Witnesses().get(0).getValue();
        assertThat(updatedWitness.getFirstName()).isEqualTo("Bob");
        assertThat(updatedWitness.getLastName()).isEqualTo("TBC");
    }

    // ---- DQ WITNESSES ----

    @Test
    void shouldUpdateAllDQWitnessesWithTBCWhenNamesAreNull() {
        Witness dqWitness1 = Witness.builder().firstName(null).lastName(null).build();
        Witness dqWitness2 = Witness.builder().firstName("Jane").lastName(null).build();

        List<Element<Witness>> dqElements = List.of(
            Element.<Witness>builder().value(dqWitness1).build(),
            Element.<Witness>builder().value(dqWitness2).build()
        );

        Witnesses dqWitnesses = Witnesses.builder().details(dqElements).build();

        CaseData caseData = CaseData.builder()
            .applicant1DQ(Applicant1DQ.builder().applicant1DQWitnesses(dqWitnesses).build())
            .applicant2DQ(Applicant2DQ.builder().applicant2DQWitnesses(dqWitnesses).build())
            .respondent1DQ(Respondent1DQ.builder().respondent1DQWitnesses(dqWitnesses).build())
            .respondent2DQ(Respondent2DQ.builder().respondent2DQWitnesses(dqWitnesses).build())
            .build();

        CaseReference ref = CaseReference.builder().caseReference("CASE123").build();
        CaseData updated = task.migrateCaseData(caseData, ref);

        List<Element<Witness>> updatedApp1 = updated.getApplicant1DQ().getApplicant1DQWitnesses().getDetails();
        List<Element<Witness>> updatedResp1 = updated.getRespondent1DQ().getRespondent1DQWitnesses().getDetails();

        assertThat(updatedApp1.get(0).getValue().getFirstName()).isEqualTo("TBC");
        assertThat(updatedApp1.get(0).getValue().getLastName()).isEqualTo("TBC");
        assertThat(updatedApp1.get(1).getValue().getLastName()).isEqualTo("TBC");
        assertThat(updatedResp1.get(0).getValue().getFirstName()).isEqualTo("TBC");
    }

    // ---- PARTY ID GENERATION ----

    @Test
    void shouldAssignNewPartyIdWhenMissing() {
        PartyFlagStructure witness = PartyFlagStructure.builder()
            .firstName("Jane")
            .lastName("Doe")
            .partyID(null)
            .build();

        CaseData caseData = CaseData.builder()
            .applicantWitnesses(List.of(Element.<PartyFlagStructure>builder().value(witness).build()))
            .build();

        CaseReference ref = CaseReference.builder().caseReference("REF-1").build();

        CaseData updated = task.migrateCaseData(caseData, ref);
        String newPartyId = updated.getApplicantWitnesses().get(0).getValue().getPartyID();

        assertThat(newPartyId)
            .isNotNull()
            .isNotBlank()
            .isNotEqualTo(witness.getPartyID());
    }

    @Test
    void shouldNotChangeExistingPartyId() {
        PartyFlagStructure witness = PartyFlagStructure.builder()
            .firstName("Alex")
            .lastName("Mason")
            .partyID("existing-id-001")
            .build();

        CaseData caseData = CaseData.builder()
            .respondent1Witnesses(List.of(Element.<PartyFlagStructure>builder().value(witness).build()))
            .build();

        CaseReference ref = CaseReference.builder().caseReference("REF-2").build();

        CaseData updated = task.migrateCaseData(caseData, ref);
        assertThat(updated.getRespondent1Witnesses().get(0).getValue().getPartyID())
            .isEqualTo("existing-id-001");
    }

    // ---- NULL HANDLING ----

    @Test
    void shouldHandleNullListsGracefully() {
        CaseData caseData = CaseData.builder()
            .applicantWitnesses(null)
            .respondent1Witnesses(null)
            .respondent2Witnesses(null)
            .applicant1DQ(null)
            .applicant2DQ(null)
            .respondent1DQ(null)
            .respondent2DQ(null)
            .build();

        CaseReference ref = CaseReference.builder().caseReference("REF-3").build();

        CaseData updated = task.migrateCaseData(caseData, ref);
        assertThat(updated.getApplicantWitnesses()).isEmpty();
        assertThat(updated.getRespondent1Witnesses()).isEmpty();
        assertThat(updated.getRespondent2Witnesses()).isEmpty();
        assertThat(updated.getApplicant1DQ()).isNull();
        assertThat(updated.getRespondent2DQ()).isNull();
    }

    @Test
    void shouldReturnExpectedMetadataValues() {
        assertThat(task.getTaskName()).isEqualTo("UpdatePartyWitnessTask");
        assertThat(task.getEventSummary()).contains("Update case party witness via migration task");
        assertThat(task.getEventDescription()).contains("UpdatePartyWitnessTask updates witness");
    }
}
