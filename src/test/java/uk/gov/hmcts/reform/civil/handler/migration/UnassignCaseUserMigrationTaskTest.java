package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseAssignmentMigrationCaseReference;
import uk.gov.hmcts.reform.civil.controllers.testingsupport.CaseAssignmentSupportService;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UnassignCaseUserMigrationTaskTest {

    @Mock
    private CaseAssignmentSupportService caseAssignmentSupportService;

    private UnassignCaseUserMigrationTask task;

    @BeforeEach
    void setUp() {
        task = new UnassignCaseUserMigrationTask(caseAssignmentSupportService);
    }

    @Test
    void shouldInvokeSupportServiceDuringMigration() {
        CaseAssignmentMigrationCaseReference reference = CaseAssignmentMigrationCaseReference.builder()
            .caseReference("1234567890123456")
            .userEmailAddress("user@example.com")
            .organisationId("ORG1")
            .build();

        CaseData caseData = CaseData.builder().ccdCaseReference(1234567890123456L).build();

        task.migrateCaseData(caseData, reference);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(caseAssignmentSupportService).unAssignUserFromCasesByEmail(
            captor.capture(),
            eq("ORG1"),
            eq("user@example.com")
        );
        assertThat(captor.getValue()).containsExactly("1234567890123456");
    }

    @Test
    void shouldExposeCaseReferenceFields() {
        CaseAssignmentMigrationCaseReference reference = CaseAssignmentMigrationCaseReference.builder()
            .caseReference("123")
            .userEmailAddress("user@example.com")
            .organisationId("ORG1")
            .build();

        assertThat(reference.getCaseReference()).isEqualTo("123");
        assertThat(reference.getUserEmailAddress()).isEqualTo("user@example.com");
        assertThat(reference.getOrganisationId()).isEqualTo("ORG1");
    }

    @Test
    void shouldExposeTaskMetadata() {
        assertThat(task.getTaskName()).isEqualTo("UnassignCaseUserMigrationTask");
        assertThat(task.getEventSummary()).isEqualTo("Unassign user from case via migration task");
        assertThat(task.getEventDescription()).contains("Removes the provided user");
    }

    @Test
    void shouldThrowWhenCaseIdMissing() {
        CaseAssignmentMigrationCaseReference reference = CaseAssignmentMigrationCaseReference.builder()
            .caseReference(null)
            .userEmailAddress("user@example.com")
            .organisationId("ORG1")
            .build();

        assertThatThrownBy(() -> task.migrateCaseData(CaseData.builder().build(), reference))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowWhenUserEmailMissing() {
        CaseAssignmentMigrationCaseReference reference = CaseAssignmentMigrationCaseReference.builder()
            .caseReference("123")
            .userEmailAddress(" ")
            .organisationId("ORG1")
            .build();

        assertThatThrownBy(() -> task.migrateCaseData(CaseData.builder().build(), reference))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowWhenOrganisationMissing() {
        CaseAssignmentMigrationCaseReference reference = CaseAssignmentMigrationCaseReference.builder()
            .caseReference("123")
            .userEmailAddress("user@example.com")
            .organisationId("")
            .build();

        assertThatThrownBy(() -> task.migrateCaseData(CaseData.builder().build(), reference))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowWhenCaseReferenceObjectMissing() {
        assertThatThrownBy(() -> task.migrateCaseData(CaseData.builder().build(), null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
