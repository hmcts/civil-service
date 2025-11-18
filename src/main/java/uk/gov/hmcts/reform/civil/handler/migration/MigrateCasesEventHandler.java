package uk.gov.hmcts.reform.civil.handler.migration;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReferenceCsvLoader;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.DashboardScenarioCaseReference;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.ExcelMappable;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.utils.CaseMigrationEncryptionUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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

        String taskName = externalTask.getVariable(TASK_NAME);
        MigrationTask<? extends CaseReference> task = migrationTaskFactory
            .getMigrationTask(taskName)
            .orElseThrow(() -> new IllegalArgumentException("No migration task found for: " + taskName));
        return handleTypedTask(externalTask, task);
    }

    @SuppressWarnings("unchecked")
    private <T extends CaseReference> ExternalTaskData handleTypedTask(ExternalTask externalTask, MigrationTask<T> task) {
        List<T> caseReferences = new ArrayList<>();
        String caseIds = externalTask.getVariable("caseIds");
        String scenario = externalTask.getVariable("scenario");

        FileValue excelFileValue = externalTask.getVariableTyped("excelFile", false);

        if (excelFileValue != null) {
            byte[] excelBytes;
            try (InputStream is = excelFileValue.getValue();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                excelBytes = baos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (ExcelMappable.class.isAssignableFrom(task.getType())) {
                caseReferences = caseReferenceCsvLoader.loadFromExcelBytes(task.getType(), excelBytes);
            }

        } else if (caseIds != null && !caseIds.isEmpty()) {
            List<String> caseIdList = Arrays.stream(caseIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

            caseReferences = caseIdList.stream()
                .map(id -> {
                    Object instance;
                    if (scenario != null) {
                        DashboardScenarioCaseReference scenarioInstance = new DashboardScenarioCaseReference();
                        scenarioInstance.setCaseReference(id);
                        scenarioInstance.setDashboardScenario(scenario);
                        instance = scenarioInstance;
                    } else {
                        CaseReference caseRef = new CaseReference();
                        caseRef.setCaseReference(id);
                        instance = caseRef;
                    }
                    return task.getType().cast(instance);
                })
                .toList();
            log.info("Created {} case references from Camunda variables", caseReferences.size());
        } else {
            log.info("caseIds or scenario are not provided. Falling back to csv check");
            // Fallback to CSV
            String csvFileName = externalTask.getVariable(CSV_FILE_NAME);
            if (csvFileName == null) {
                throw new IllegalArgumentException("csvFileName is missing and no caseIds provided");
            }
            caseReferences = getCaseReferenceList(task.getType(), csvFileName);
        }

        if (caseReferences.isEmpty()) {
            log.warn("No case references found to process");
            return ExternalTaskData.builder().build();
        }

        log.info("Found {} case references to process", caseReferences.size());

        String state = externalTask.getVariable("state");
        asyncCaseMigrationService.migrateCasesAsync(task, caseReferences, state);

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
