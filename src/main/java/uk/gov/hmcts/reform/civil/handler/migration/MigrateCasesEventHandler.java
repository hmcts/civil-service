package uk.gov.hmcts.reform.civil.handler.migration;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReferenceCsvLoader;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.utils.CaseMigrationEncryptionUtil;

import java.util.List;

@Component
@Slf4j
public class MigrateCasesEventHandler extends BaseExternalTaskHandler {

    protected static final String TASK_NAME = "taskName";
    protected static final String CSV_FILE_NAME = "csvFileName";
    private final CaseReferenceCsvLoader caseReferenceCsvLoader;
    private final MigrationTaskFactory migrationTaskFactory;
    private final AsyncCaseMigrationService asyncCaseMigrationService;
    private final String encryptionSecret;

    public MigrateCasesEventHandler(
        CaseReferenceCsvLoader caseReferenceCsvLoader,
        MigrationTaskFactory migrationTaskFactory,
        AsyncCaseMigrationService asyncCaseMigrationService,
        @Value("${migration.csvFile.decrypt.key:DUMMY_KEY}") String encryptionSecret

    ) {
        this.caseReferenceCsvLoader = caseReferenceCsvLoader;
        this.migrationTaskFactory = migrationTaskFactory;
        this.encryptionSecret = encryptionSecret;
        this.asyncCaseMigrationService = asyncCaseMigrationService;
    }

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        assert externalTask.getVariable(TASK_NAME) != null;
        if (externalTask.getVariable(CSV_FILE_NAME) == null) {
            throw new AssertionError("csvFileName is null");
        }
        String taskName = externalTask.getVariable(TASK_NAME);
        MigrationTask<? extends CaseReference> task = migrationTaskFactory
            .getMigrationTask(taskName)
            .orElseThrow(() -> new IllegalArgumentException("No migration task found for: " + taskName));
        return handleTypedTask(externalTask, task);
    }

    private <T extends CaseReference> ExternalTaskData handleTypedTask(ExternalTask externalTask, MigrationTask<T> task) {
        String csvFileName = externalTask.getVariable(CSV_FILE_NAME);
        List<T> caseReferences = getCaseReferenceList(task.getType(), csvFileName);
        log.info("Found {} case references to process", caseReferences.size());
        if (caseReferences.isEmpty()) {
            log.warn("No case references found to process");
            return ExternalTaskData.builder().build();
        }
        asyncCaseMigrationService.migrateCasesAsync(task, caseReferences);

        return ExternalTaskData.builder().build();
    }

    protected <T extends CaseReference> List<T> getCaseReferenceList(Class<T> type, String csvFileName) {
        log.info("Loading case references from CSV file: {}", csvFileName);
        if (CaseMigrationEncryptionUtil.isFileEncrypted(csvFileName)) {
            return caseReferenceCsvLoader.loadCaseReferenceList(type, csvFileName, encryptionSecret);
        } else {
            return caseReferenceCsvLoader.loadCaseReferenceList(type, csvFileName);
        }
    }
}
