package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.HousingDisrepair;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MigrateFastTrackHousingDisrepairTaskTest {

    private MigrateFastTrackHousingDisrepairTask task;

    @BeforeEach
    void setUp() {
        task = new MigrateFastTrackHousingDisrepairTask();
    }

    @Test
    void shouldReturnCorrectTaskName() {
        assertThat(task.getTaskName()).isEqualTo("MigrateFastTrackHousingDisrepairTask");
    }

    @Test
    void shouldReturnCorrectEventSummary() {
        assertThat(task.getEventSummary())
            .isEqualTo("Migrate to new fastTrack housing disrepair via migration task");
    }

    @Test
    void shouldReturnCorrectEventDescription() {
        assertThat(task.getEventDescription())
            .isEqualTo("This task migrates new fastTrack housing disrepair on the case");
    }

    @Test
    void shouldDeclareCaseReferenceType() {
        assertThat(task.getType()).isEqualTo(CaseReference.class);
    }

    @Test
    void shouldMigrateLegacyFastTrackHousingDisrepairFields() {
        LocalDate firstReportDate = LocalDate.of(2026, 1, 15);
        LocalDate jointStatementDate = LocalDate.of(2026, 2, 20);
        HousingDisrepair existingHousingDisrepair = new HousingDisrepair();
        existingHousingDisrepair.setInput1("Legacy clause A");
        existingHousingDisrepair.setInput2("Legacy clause B");
        existingHousingDisrepair.setInput3("Legacy clause D");
        existingHousingDisrepair.setInput4("Legacy clause E");
        existingHousingDisrepair.setDate1(firstReportDate);
        existingHousingDisrepair.setDate2(jointStatementDate);

        CaseData caseData = CaseData.builder()
            .fastTrackHousingDisrepair(existingHousingDisrepair)
            .build();

        CaseData result = task.migrateCaseData(caseData, new CaseReference("1234567890123456"));

        assertThat(result).isSameAs(caseData);

        HousingDisrepair migratedHousingDisrepair = result.getFastTrackHousingDisrepair();
        assertThat(migratedHousingDisrepair)
            .isNotNull()
            .isNotSameAs(existingHousingDisrepair);
        assertThat(migratedHousingDisrepair.getClauseA()).isEqualTo("Legacy clause A");
        assertThat(migratedHousingDisrepair.getClauseB()).isEqualTo("Legacy clause B");
        assertThat(migratedHousingDisrepair.getClauseD()).isEqualTo("Legacy clause D");
        assertThat(migratedHousingDisrepair.getClauseE()).isEqualTo("Legacy clause E");
        assertThat(migratedHousingDisrepair.getFirstReportDateBy()).isEqualTo(firstReportDate);
        assertThat(migratedHousingDisrepair.getJointStatementDateBy()).isEqualTo(jointStatementDate);
        assertThat(migratedHousingDisrepair.getInput1()).isNull();
        assertThat(migratedHousingDisrepair.getInput2()).isNull();
        assertThat(migratedHousingDisrepair.getInput3()).isNull();
        assertThat(migratedHousingDisrepair.getInput4()).isNull();
        assertThat(migratedHousingDisrepair.getDate1()).isNull();
        assertThat(migratedHousingDisrepair.getDate2()).isNull();
    }
}
