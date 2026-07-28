package uk.gov.hmcts.reform.civil.handler.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.DocumentPurgeReference;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.UserService;

/**
 * One-off remediation migration task that hard-deletes an orphaned document binary from the
 * document store (CDAM / dm-store) by id.
 *
 * <p>It is used to purge documents that were removed from a case via the {@code REMOVE_DOCUMENT}
 * event while {@code DOCSTORE_DOC_REMOVAL_ENABLED} was off, so the document reference was detached
 * from the case data but the binary was never deleted from the store.</p>
 *
 * <p>The task is {@link #isReadOnly() read-only with respect to the case}: it makes no change to
 * CaseData and writes no CCD event / case-history entry. Its only side effect is the permanent
 * deletion of the target binary from the document store, delegated to
 * {@link DocumentManagementService#deleteDocument} (which calls CDAM with the permanent flag).
 * It emits one structured log line per document (prefix {@code PURGE_DOCSTORE_DOCUMENT}) for
 * filtering in App Insights.</p>
 *
 * <p>Trigger via the CSV path with columns {@code caseReference,documentId} (see
 * {@code DocumentPurgeReference}); the {@code documentId} is the document store id (UUID).</p>
 */
@Component
@Slf4j
public class PurgeDocStoreDocumentTask extends MigrationTask<DocumentPurgeReference> {

    private static final String LOG_PREFIX = "PURGE_DOCSTORE_DOCUMENT";
    private static final String TASK_NAME = "PurgeDocStoreDocumentTask";
    private static final String EVENT_SUMMARY = "Purge orphaned document from the document store";
    private static final String EVENT_DESCRIPTION =
        "Read-only remediation task that hard-deletes an orphaned document binary from the document store by id";

    private final DocumentManagementService documentManagementService;
    private final UserService userService;
    private final SystemUpdateUserConfiguration userConfig;

    public PurgeDocStoreDocumentTask(DocumentManagementService documentManagementService,
                                     UserService userService,
                                     SystemUpdateUserConfiguration userConfig) {
        super(DocumentPurgeReference.class);
        this.documentManagementService = documentManagementService;
        this.userService = userService;
        this.userConfig = userConfig;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, DocumentPurgeReference reference) {
        if (reference == null || reference.getCaseReference() == null
            || reference.getDocumentId() == null || reference.getDocumentId().isBlank()) {
            throw new IllegalArgumentException("CaseReference and documentId must not be null or blank");
        }

        String caseId = reference.getCaseReference();
        String documentId = reference.getDocumentId().trim();

        String authorisation = getSystemUserToken();
        documentManagementService.deleteDocument(authorisation, documentId);
        log.info("{} case={} documentId={} result=DELETED", LOG_PREFIX, caseId, documentId);

        return caseData;
    }

    @Override
    protected String getTaskName() {
        return TASK_NAME;
    }

    @Override
    protected String getEventSummary() {
        return EVENT_SUMMARY;
    }

    @Override
    protected String getEventDescription() {
        return EVENT_DESCRIPTION;
    }

    private String getSystemUserToken() {
        return userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
    }
}
