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
            case INDIVIDUAL -> new Party()
                .setType(Party.Type.INDIVIDUAL)
                .setIndividualFirstName("OldFirst")
                .setIndividualLastName("OldLast")
                .setPartyEmail("old@example.com")
                .setIndividualDateOfBirth(LocalDate.of(1990, 1, 1));
            case SOLE_TRADER -> new Party()
                .setType(Party.Type.SOLE_TRADER)
                .setSoleTraderFirstName("OldFirst")
                .setSoleTraderLastName("OldLast");
            case COMPANY -> new Party()
                .setType(Party.Type.COMPANY)
                .setCompanyName("OldCompany");
            case ORGANISATION -> new Party()
                .setType(Party.Type.ORGANISATION)
                .setOrganisationName("OldOrg");
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
            case INDIVIDUAL -> new Party()
                .setIndividualFirstName("NewFirst")
                .setIndividualLastName("NewLast")
                .setPartyEmail("new@example.com");
            case SOLE_TRADER -> new Party()
                .setSoleTraderFirstName("NewFirst")
                .setSoleTraderLastName("NewLast");
            case COMPANY -> new Party()
                .setCompanyName("NewCompany");
            case ORGANISATION -> new Party()
                .setOrganisationName("NewOrg");
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
        ref.setParty(new Party());

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> task.migrateCaseData(caseData, ref)
        );
        assertEquals("Failed to determine Party to update", ex.getMessage());
    }

    @Test
    void migrateCaseData_shouldReturnOriginalPartyIfUpdatesAreNull() {
        Party existing = new Party()
            .setIndividualFirstName("John");

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
