package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.DocumentPurgeReference;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PurgeDocStoreDocumentTaskTest {

    private static final String CASE_ID = "1770925621123458";
    private static final String DOC_ID = "6c6403c1-ad61-4c60-9ffa-852ea0b25a7e";

    private DocumentManagementService documentManagementService;
    private UserService userService;
    private SystemUpdateUserConfiguration userConfig;
    private PurgeDocStoreDocumentTask task;

    @BeforeEach
    void setUp() {
        documentManagementService = mock(DocumentManagementService.class);
        userService = mock(UserService.class);
        userConfig = mock(SystemUpdateUserConfiguration.class);
        task = new PurgeDocStoreDocumentTask(documentManagementService, userService, userConfig);
    }

    private DocumentPurgeReference reference(String caseId, String documentId) {
        DocumentPurgeReference ref = new DocumentPurgeReference();
        ref.setCaseReference(caseId);
        ref.setDocumentId(documentId);
        return ref;
    }

    @Test
    void shouldReturnCorrectTaskName() {
        assertThat(task.getTaskName()).isEqualTo("PurgeDocStoreDocumentTask");
    }

    @Test
    void shouldReturnCorrectEventSummary() {
        assertThat(task.getEventSummary()).isEqualTo("Purge orphaned document from the document store");
    }

    @Test
    void shouldReturnEventDescription() {
        assertThat(task.getEventDescription()).contains("hard-deletes an orphaned document");
    }

    @Test
    void shouldBeReadOnly() {
        assertThat(task.isReadOnly()).isTrue();
    }

    @Test
    void shouldDeleteDocumentUsingSystemUserToken() {
        when(userConfig.getUserName()).thenReturn("system-user");
        when(userConfig.getPassword()).thenReturn("pass");
        when(userService.getAccessToken("system-user", "pass")).thenReturn("Bearer token");
        CaseData caseData = CaseData.builder().build();

        CaseData result = task.migrateCaseData(caseData, reference(CASE_ID, DOC_ID));

        verify(documentManagementService).deleteDocument("Bearer token", DOC_ID);
        assertThat(result).isSameAs(caseData);
    }

    @Test
    void shouldTrimDocumentIdBeforeDeleting() {
        when(userConfig.getUserName()).thenReturn("system-user");
        when(userConfig.getPassword()).thenReturn("pass");
        when(userService.getAccessToken("system-user", "pass")).thenReturn("token");

        task.migrateCaseData(CaseData.builder().build(), reference(CASE_ID, "  " + DOC_ID + "  "));

        verify(documentManagementService).deleteDocument("token", DOC_ID);
    }

    @Test
    void shouldThrowAndNotDeleteWhenDocumentIdMissing() {
        assertThatThrownBy(() -> task.migrateCaseData(CaseData.builder().build(), reference(CASE_ID, null)))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> task.migrateCaseData(CaseData.builder().build(), reference(CASE_ID, "  ")))
            .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(documentManagementService);
    }
}
