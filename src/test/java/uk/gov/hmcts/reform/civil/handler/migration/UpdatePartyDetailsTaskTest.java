package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.PartyDetailsCaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdatePartyDetailsTaskTest {

    private UpdatePartyDetailsTask task;

    @BeforeEach
    void setUp() {
        task = new UpdatePartyDetailsTask();
    }

    @Test
    void migrateCaseData_shouldUpdateApplicant1Party() {
        Party existing = Party.builder()
            .individualFirstName("OldFirst")
            .individualLastName("OldLast")
            .individualDateOfBirth(LocalDate.of(1990, 1, 1))
            .type(Party.Type.INDIVIDUAL)
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1(existing)
            .build();

        Party updates = Party.builder()
            .individualFirstName("NewFirst")
            .individualLastName("NewLast")
            .partyEmail("new@example.com")
            .build();

        PartyDetailsCaseReference caseRef = new PartyDetailsCaseReference();
        caseRef.setParty(updates);
        caseRef.setCaseReference("123");
        caseRef.setApplicant1(true);

        CaseData result = task.migrateCaseData(caseData, caseRef);
        Party resultParty = result.getApplicant1();

        assertEquals("NewFirst", resultParty.getIndividualFirstName());
        assertEquals("NewLast", resultParty.getIndividualLastName());
        assertEquals("new@example.com", resultParty.getPartyEmail());

        assertEquals(LocalDate.of(1990, 1, 1), resultParty.getIndividualDateOfBirth());
    }

    @Test
    void migrateCaseData_shouldThrow_whenCaseReferenceIsNull() {
        CaseData caseData = CaseData.builder().build();

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> task.migrateCaseData(caseData, null)
        );

        assertEquals("CaseReference fields must not be null", ex.getMessage());
    }

    @Test
    void migrateCaseData_shouldThrow_whenPartyNotFound() {
        CaseData caseData = CaseData.builder().build();

        PartyDetailsCaseReference caseRef = new PartyDetailsCaseReference();
        caseRef.setParty(Party.builder().build());
        caseRef.setCaseReference("123");
        caseRef.setApplicant1(true);

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> task.migrateCaseData(caseData, caseRef)
        );

        assertEquals("Failed to determine Party to update", ex.getMessage());
    }

    @Test
    void migrateCaseData_shouldUpdateOnlyNonNullFields() {
        Party existing = Party.builder()
            .individualFirstName("OldFirst")
            .individualLastName("OldLast")
            .partyEmail("old@example.com")
            .type(Party.Type.INDIVIDUAL)
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1(existing)
            .build();

        Party updates = Party.builder()
            .individualFirstName(null)
            .individualLastName("NewLast")
            .partyEmail("  ")
            .build();

        PartyDetailsCaseReference caseRef = new PartyDetailsCaseReference();
        caseRef.setParty(updates);
        caseRef.setCaseReference("123");
        caseRef.setApplicant1(true);

        CaseData result = task.migrateCaseData(caseData, caseRef);
        Party resultParty = result.getApplicant1();

        assertEquals("OldFirst", resultParty.getIndividualFirstName());
        assertEquals("NewLast", resultParty.getIndividualLastName());
        assertEquals("old@example.com", resultParty.getPartyEmail());
    }
}
