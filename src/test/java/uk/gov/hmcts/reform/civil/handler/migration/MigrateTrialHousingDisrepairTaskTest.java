package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHousingDisrepair;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MigrateTrialHousingDisrepairTaskTest {

    private MigrateTrialHousingDisrepairTask task;

    @BeforeEach
    void setUp() {
        task = new MigrateTrialHousingDisrepairTask();
    }

    @Test
    void shouldReturnCorrectTaskName() {
        assertThat(task.getTaskName()).isEqualTo("MigrateTrialHousingDisrepairTask");
    }

    @Test
    void shouldReturnCorrectEventSummary() {
        assertThat(task.getEventSummary())
            .isEqualTo("Migrate to trail housing disrepair via migration task");
    }

    @Test
    void shouldReturnCorrectEventDescription() {
        assertThat(task.getEventDescription())
            .isEqualTo("This task migrates trail housing disrepair on the case");
    }

    @Test
    void shouldDeclareCaseReferenceType() {
        assertThat(task.getType()).isEqualTo(CaseReference.class);
    }

    @Test
    void shouldMigrateLegacyTrialHousingDisrepairFields() {
        LocalDate firstReportDate = LocalDate.of(2026, 1, 15);
        LocalDate jointStatementDate = LocalDate.of(2026, 2, 20);
        TrialHousingDisrepair existingTrialHousingDisrepair = new TrialHousingDisrepair();
        existingTrialHousingDisrepair.setInput1("Legacy clause A");
        existingTrialHousingDisrepair.setInput2("Legacy clause B");
        existingTrialHousingDisrepair.setInput3("Legacy clause D");
        existingTrialHousingDisrepair.setInput4("Legacy clause E");
        existingTrialHousingDisrepair.setDate1(firstReportDate);
        existingTrialHousingDisrepair.setDate2(jointStatementDate);

        CaseData caseData = CaseData.builder()
            .trialHousingDisrepair(existingTrialHousingDisrepair)
            .build();

        CaseData result = task.migrateCaseData(caseData, new CaseReference("1234567890123456"));

        assertThat(result).isSameAs(caseData);

        TrialHousingDisrepair migratedTrialHousingDisrepair = result.getTrialHousingDisrepair();
        assertThat(migratedTrialHousingDisrepair)
            .isNotNull()
            .isNotSameAs(existingTrialHousingDisrepair);
        assertThat(migratedTrialHousingDisrepair.getClauseA()).isEqualTo("Legacy clause A");
        assertThat(migratedTrialHousingDisrepair.getClauseB()).isEqualTo("Legacy clause B");
        assertThat(migratedTrialHousingDisrepair.getClauseD()).isEqualTo("Legacy clause D");
        assertThat(migratedTrialHousingDisrepair.getClauseE()).isEqualTo("Legacy clause E");
        assertThat(migratedTrialHousingDisrepair.getFirstReportDateBy()).isEqualTo(firstReportDate);
        assertThat(migratedTrialHousingDisrepair.getJointStatementDateBy()).isEqualTo(jointStatementDate);
        assertThat(migratedTrialHousingDisrepair.getInput1()).isNull();
        assertThat(migratedTrialHousingDisrepair.getInput2()).isNull();
        assertThat(migratedTrialHousingDisrepair.getInput3()).isNull();
        assertThat(migratedTrialHousingDisrepair.getInput4()).isNull();
        assertThat(migratedTrialHousingDisrepair.getDate1()).isNull();
        assertThat(migratedTrialHousingDisrepair.getDate2()).isNull();
    }
}
