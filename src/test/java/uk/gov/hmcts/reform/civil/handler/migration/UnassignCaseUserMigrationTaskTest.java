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
    void shouldThrowWhenMandatoryFieldsMissing() {
        CaseAssignmentMigrationCaseReference reference = CaseAssignmentMigrationCaseReference.builder()
            .caseReference(null)
            .userEmailAddress(null)
            .organisationId(null)
            .build();

        assertThatThrownBy(() -> task.migrateCaseData(CaseData.builder().build(), reference))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
