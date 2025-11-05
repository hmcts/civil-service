package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.PartyDetailsCaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdatePartyDetailsTaskTest {

    private final UpdatePartyDetailsTask task = new UpdatePartyDetailsTask();

    record TestScenario(String roleField, Party.Type partyType) {}

    static Stream<TestScenario> provideScenarios() {
        return Stream.of(
            new TestScenario("applicant1", Party.Type.INDIVIDUAL),
            new TestScenario("applicant2", Party.Type.INDIVIDUAL),
            new TestScenario("respondent1", Party.Type.INDIVIDUAL),
            new TestScenario("respondent2", Party.Type.INDIVIDUAL),
            new TestScenario("applicant1", Party.Type.SOLE_TRADER),
            new TestScenario("applicant2", Party.Type.SOLE_TRADER),
            new TestScenario("respondent1", Party.Type.COMPANY),
            new TestScenario("respondent2", Party.Type.ORGANISATION)
        );
    }

    @ParameterizedTest
    @MethodSource("provideScenarios")
    void migrateCaseData_shouldUpdateCorrectRoleAndParty(TestScenario scenario) {
        Party existing = switch (scenario.partyType) {
            case INDIVIDUAL -> Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("OldFirst")
                .individualLastName("OldLast")
                .partyEmail("old@example.com")
                .individualDateOfBirth(LocalDate.of(1990, 1, 1))
                .build();
            case SOLE_TRADER -> Party.builder()
                .type(Party.Type.SOLE_TRADER)
                .soleTraderFirstName("OldFirst")
                .soleTraderLastName("OldLast")
                .build();
            case COMPANY -> Party.builder()
                .type(Party.Type.COMPANY)
                .companyName("OldCompany")
                .build();
            case ORGANISATION -> Party.builder()
                .type(Party.Type.ORGANISATION)
                .organisationName("OldOrg")
                .build();
        };

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();
        switch (scenario.roleField) {
            case "applicant1" -> caseDataBuilder.applicant1(existing);
            case "applicant2" -> caseDataBuilder.applicant2(existing);
            case "respondent1" -> caseDataBuilder.respondent1(existing);
            case "respondent2" -> caseDataBuilder.respondent2(existing);
            default -> {

            }
        }

        Party updates = switch (scenario.partyType) {
            case INDIVIDUAL -> Party.builder()
                .individualFirstName("NewFirst")
                .individualLastName("NewLast")
                .partyEmail("new@example.com")
                .build();
            case SOLE_TRADER -> Party.builder()
                .soleTraderFirstName("NewFirst")
                .soleTraderLastName("NewLast")
                .build();
            case COMPANY -> Party.builder()
                .companyName("NewCompany")
                .build();
            case ORGANISATION -> Party.builder()
                .organisationName("NewOrg")
                .build();
        };

        PartyDetailsCaseReference ref = new PartyDetailsCaseReference();
        ref.setCaseReference("1234567890");
        ref.setParty(updates);
        switch (scenario.roleField) {
            case "applicant1" -> ref.setApplicant1(true);
            case "applicant2" -> ref.setApplicant2(true);
            case "respondent1" -> ref.setRespondent1(true);
            case "respondent2" -> ref.setRespondent2(true);
            default -> {

            }
        }

        CaseData caseData = caseDataBuilder.build();
        CaseData result = task.migrateCaseData(caseData, ref);
        Party updated = switch (scenario.roleField) {
            case "applicant1" -> result.getApplicant1();
            case "applicant2" -> result.getApplicant2();
            case "respondent1" -> result.getRespondent1();
            case "respondent2" -> result.getRespondent2();
            default -> throw new IllegalStateException("Unexpected value: " + scenario.roleField);
        };

        switch (scenario.partyType) {
            case INDIVIDUAL -> {
                assertEquals("NewFirst", updated.getIndividualFirstName());
                assertEquals("NewLast", updated.getIndividualLastName());
                assertEquals("new@example.com", updated.getPartyEmail());
                assertEquals(LocalDate.of(1990, 1, 1), updated.getIndividualDateOfBirth());
            }
            case SOLE_TRADER -> {
                assertEquals("NewFirst", updated.getSoleTraderFirstName());
                assertEquals("NewLast", updated.getSoleTraderLastName());
            }
            case COMPANY -> assertEquals("NewCompany", updated.getCompanyName());
            case ORGANISATION -> assertEquals("NewOrg", updated.getOrganisationName());
            default -> {

            }
        }
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
    void migrateCaseData_shouldThrow_whenPartyRoleNotFound() {
        CaseData caseData = CaseData.builder().build();
        PartyDetailsCaseReference ref = new PartyDetailsCaseReference();
        ref.setCaseReference("123");
        ref.setParty(Party.builder().build());

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> task.migrateCaseData(caseData, ref)
        );
        assertEquals("Failed to determine Party to update", ex.getMessage());
    }

    @Test
    void migrateCaseData_shouldReturnOriginalPartyIfUpdatesAreNull() {
        Party existing = Party.builder()
            .individualFirstName("John")
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1(existing)
            .build();

        PartyDetailsCaseReference ref = new PartyDetailsCaseReference();
        ref.setCaseReference("123456");
        ref.setApplicant1(true);
        ref.setParty(null);

        CaseData updatedCaseData = task.migrateCaseData(caseData, ref);
        assertSame(existing, updatedCaseData.getApplicant1());
    }
}
