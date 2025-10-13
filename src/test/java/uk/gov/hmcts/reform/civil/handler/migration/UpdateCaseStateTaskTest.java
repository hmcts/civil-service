package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateCaseStateTaskTest {

    private UpdateCaseStateTask updateCaseStateTask;

    @BeforeEach
    void setUp() {
        updateCaseStateTask = new UpdateCaseStateTask();
    }

    @Test
    void shouldReturnCorrectTaskName() {
        String taskName = updateCaseStateTask.getTaskName();
        assertThat(taskName).isEqualTo("UpdateCaseStateTask");
    }

    @Test
    void shouldReturnCorrectEventSummary() {
        String summary = updateCaseStateTask.getEventSummary();
        assertThat(summary).isEqualTo("Update case state via migration task");
    }

    @Test
    void shouldReturnCorrectEventDescription() {
        String description = updateCaseStateTask.getEventDescription();
        assertThat(description).isEqualTo("This task update state on the case");
    }

    @Test
    void migrateCaseData_shouldReturnSameCaseData() {
        CaseData caseData = CaseData.builder().build();
        CaseReference caseReference = new CaseReference("12345");

        CaseData result = updateCaseStateTask.migrateCaseData(caseData, caseReference);

        assertThat(result).isSameAs(caseData);
    }
}
