package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.GeneralApplicationCaseNameService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncGeneralApplicationCaseNameTaskTest {

    @Mock
    private GeneralApplicationCaseNameService generalApplicationCaseNameService;

    @InjectMocks
    private SyncGeneralApplicationCaseNameTask task;

    @Test
    void shouldUpdateLinkedGeneralApplicationsAndLeaveParentUnchanged() {
        CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(1720528956400235L).build();
        caseData.setCaseNameHmctsInternal("Claimant v Original Defendant");

        CaseData result = task.migrateCaseData(caseData, new CaseReference("1720528956400235"));

        verify(generalApplicationCaseNameService).updateGeneralApplications(caseData);
        assertThat(result).isSameAs(caseData);
        assertThat(result.getCaseNameHmctsInternal()).isEqualTo("Claimant v Original Defendant");
    }

    @Test
    void shouldThrowWithFailedReferences_whenAnyGeneralApplicationUpdateFails() {
        CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(1720528956400235L).build();
        caseData.setCaseNameHmctsInternal("Claimant v Original Defendant");
        CaseReference caseReference = new CaseReference("1720528956400235");
        when(generalApplicationCaseNameService.updateGeneralApplications(caseData)).thenReturn(List.of("111", "333"));

        IllegalStateException exception = assertThrows(
            IllegalStateException.class, () -> task.migrateCaseData(caseData, caseReference));

        assertThat(exception.getMessage()).contains("[111, 333]").contains("1720528956400235");
    }

    @Test
    void shouldThrowException_whenParentCaseNameIsMissing() {
        CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(1720528956400235L).build();
        caseData.setCaseNameHmctsInternal(null);
        CaseReference caseReference = new CaseReference("1720528956400235");

        assertThrows(IllegalStateException.class, () -> task.migrateCaseData(caseData, caseReference));
        verifyNoInteractions(generalApplicationCaseNameService);
    }

    @Test
    void shouldNotSubmitAnEventOnTheParentCase() {
        assertThat(task.isReadOnly()).isTrue();
    }

    @Test
    void shouldThrowException_whenCaseDataIsNull() {
        CaseReference caseReference = new CaseReference("1720528956400235");

        assertThrows(IllegalArgumentException.class, () -> task.migrateCaseData(null, caseReference));
        verifyNoInteractions(generalApplicationCaseNameService);
    }

    @Test
    void shouldThrowException_whenCaseReferenceIsNull() {
        CaseData caseData = CaseDataBuilder.builder().build();

        assertThrows(IllegalArgumentException.class, () -> task.migrateCaseData(caseData, null));
        verifyNoInteractions(generalApplicationCaseNameService);
    }

    @Test
    void shouldExposeTaskName() {
        assertThat(task.getTaskName()).isEqualTo("SyncGeneralApplicationCaseNameTask");
    }
}
