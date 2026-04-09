package uk.gov.hmcts.reform.civil.handler.migration;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseNoteReference;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReferenceCsvLoader;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.DashboardScenarioCaseReference;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.ExcelMappable;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.NotificationCaseReference;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.NotifyRpaFeedCaseReference;
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
    private static final String CASE_IDS = "caseIds";
    private static final String SCENARIO = "scenario";
    private static final String NOTIFICATION_CAMUNDA_PROCESS_IDENTIFIER = "notificationCamundaProcessIdentifier";
    private static final String CASE_NOTE_ELEMENT_ID = "caseNoteElementId";
    private static final String NOTIFY_EVENT_ID = "notifyEventId";
    private static final String EXCEL_FILE = "excelFile";
    private static final String STATE = "state";
    private static final String IS_GA_CASE = "isGACase";
    private static final int EXCEL_BUFFER_SIZE = 8192;
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
        if (externalTask.getVariable(TASK_NAME) == null) {
            throw new IllegalArgumentException("Taskname can't be empty");
        }

        String taskName = externalTask.getVariable(TASK_NAME);
        MigrationTask<? extends CaseReference> task = migrationTaskFactory
            .getMigrationTask(taskName)
            .orElseThrow(() -> new IllegalArgumentException("No migration task found for: " + taskName));
        return handleTypedTask(externalTask, task);
    }

    private <T extends CaseReference> ExternalTaskData handleTypedTask(ExternalTask externalTask, MigrationTask<T> task) {
        List<T> caseReferences = resolveCaseReferences(externalTask, task);

        if (caseReferences.isEmpty()) {
            log.warn("No case references found to process");
            return new ExternalTaskData();
        }

        log.info("Found {} case references to process", caseReferences.size());

        String state = externalTask.getVariable(STATE);
        String isGACase = externalTask.getVariable(IS_GA_CASE);
        asyncCaseMigrationService.migrateCasesAsync(task, caseReferences, state, isGACase != null);

        return new ExternalTaskData();
    }

    private <T extends CaseReference> List<T> resolveCaseReferences(ExternalTask externalTask, MigrationTask<T> task) {
        FileValue excelFileValue = externalTask.getVariableTyped(EXCEL_FILE, false);
        if (excelFileValue != null) {
            return getCaseReferencesFromExcel(task, excelFileValue);
        }

        String caseIds = externalTask.getVariable(CASE_IDS);
        if (caseIds != null && !caseIds.isEmpty()) {
            return getCaseReferencesFromVariables(externalTask, task, caseIds);
        }

        return getCaseReferencesFromCsv(externalTask, task.getType());
    }

    private <T extends CaseReference> List<T> getCaseReferencesFromExcel(MigrationTask<T> task, FileValue excelFileValue) {
        if (!ExcelMappable.class.isAssignableFrom(task.getType())) {
            return new ArrayList<>();
        }
        return caseReferenceCsvLoader.loadFromExcelBytes(task.getType(), readExcelBytes(excelFileValue));
    }

    private byte[] readExcelBytes(FileValue excelFileValue) {
        try (InputStream is = excelFileValue.getValue();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[EXCEL_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends CaseReference> List<T> getCaseReferencesFromVariables(
        ExternalTask externalTask,
        MigrationTask<T> task,
        String caseIds
    ) {
        String scenario = externalTask.getVariable(SCENARIO);
        String camundaProcessIdentifier = externalTask.getVariable(NOTIFICATION_CAMUNDA_PROCESS_IDENTIFIER);
        String caseNoteElementId = externalTask.getVariable(CASE_NOTE_ELEMENT_ID);
        String notifyEventId = externalTask.getVariable(NOTIFY_EVENT_ID);

        List<T> caseReferences = Arrays.stream(caseIds.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(id -> buildCaseReference(task.getType(), id, scenario, camundaProcessIdentifier, caseNoteElementId, notifyEventId))
            .toList();
        log.info("Created {} case references from Camunda variables", caseReferences.size());
        return caseReferences;
    }

    private <T extends CaseReference> T buildCaseReference(
        Class<T> type,
        String caseId,
        String scenario,
        String camundaProcessIdentifier,
        String caseNoteElementId,
        String notifyEventId
    ) {
        if (scenario != null) {
            return type.cast(buildScenarioCaseReference(caseId, scenario));
        }
        if (camundaProcessIdentifier != null) {
            return type.cast(buildNotificationCaseReference(caseId, camundaProcessIdentifier));
        }
        if (caseNoteElementId != null) {
            return type.cast(buildCaseNoteReference(caseId, caseNoteElementId));
        }
        if (notifyEventId != null) {
            return type.cast(buildNotifyRpaFeedCaseReference(caseId, notifyEventId));
        }
        return type.cast(buildBaseCaseReference(caseId));
    }

    private DashboardScenarioCaseReference buildScenarioCaseReference(String caseId, String scenario) {
        DashboardScenarioCaseReference scenarioCaseReference = new DashboardScenarioCaseReference();
        scenarioCaseReference.setCaseReference(caseId);
        scenarioCaseReference.setDashboardScenario(scenario);
        return scenarioCaseReference;
    }

    private NotificationCaseReference buildNotificationCaseReference(String caseId, String camundaProcessIdentifier) {
        NotificationCaseReference notificationCaseReference = new NotificationCaseReference();
        notificationCaseReference.setCaseReference(caseId);
        notificationCaseReference.setCamundaProcessIdentifier(camundaProcessIdentifier);
        return notificationCaseReference;
    }

    private CaseNoteReference buildCaseNoteReference(String caseId, String caseNoteElementId) {
        CaseNoteReference caseNoteReference = new CaseNoteReference();
        caseNoteReference.setCaseReference(caseId);
        caseNoteReference.setCaseNoteElementId(caseNoteElementId);
        return caseNoteReference;
    }

    private NotifyRpaFeedCaseReference buildNotifyRpaFeedCaseReference(String caseId, String notifyEventId) {
        NotifyRpaFeedCaseReference notifyRpaFeedCaseReference = new NotifyRpaFeedCaseReference();
        notifyRpaFeedCaseReference.setCaseReference(caseId);
        notifyRpaFeedCaseReference.setNotifyEventId(notifyEventId);
        return notifyRpaFeedCaseReference;
    }

    private CaseReference buildBaseCaseReference(String caseId) {
        CaseReference caseReference = new CaseReference();
        caseReference.setCaseReference(caseId);
        return caseReference;
    }

    private <T extends CaseReference> List<T> getCaseReferencesFromCsv(ExternalTask externalTask, Class<T> type) {
        log.info("caseIds or scenario are not provided. Falling back to csv check");
        String csvFileName = externalTask.getVariable(CSV_FILE_NAME);
        if (csvFileName == null) {
            throw new IllegalArgumentException("csvFileName is missing and no caseIds provided");
        }
        return getCaseReferenceList(type, csvFileName);
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
