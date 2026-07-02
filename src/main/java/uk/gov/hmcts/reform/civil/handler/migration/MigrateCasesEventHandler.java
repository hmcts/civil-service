package uk.gov.hmcts.reform.civil.handler.migration;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseNoteReference;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReferenceCsvLoader;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.DashboardNotificationTaskCaseReference;
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
import java.util.stream.IntStream;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

@Component
@Slf4j
public class MigrateCasesEventHandler extends BaseExternalTaskHandler {

    protected static final String TASK_NAME = "taskName";
    protected static final String CSV_FILE_NAME = "csvFileName";
    private static final String CASE_IDS = "caseIds";
    private static final String SCENARIO = "scenario";
    private static final String DASHBOARD_TASK_ID = "dashboardTaskId";
    private static final String DASHBOARD_PROCESS_INSTANCE_ID = "dashboardProcessInstanceId";
    private static final String DASHBOARD_PROCESS_INSTANCE_IDS = "dashboardProcessInstanceIds";
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
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        CaseReferenceCsvLoader caseReferenceCsvLoader,
        MigrationTaskFactory migrationTaskFactory,
        AsyncCaseMigrationService asyncCaseMigrationService,
        @Value("${migration.csvFile.decrypt.key:DUMMY_KEY}") String encryptionSecret
    ) {
        super(externalTaskCompletionService, eventProperties);
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
        MigrationVariables variables = migrationVariables(externalTask);

        FileValue excelFileValue = externalTask.getVariableTyped(EXCEL_FILE, false);
        if (excelFileValue != null) {
            return getCaseReferencesFromExcel(task, excelFileValue);
        }

        if (variables.hasCaseIds()) {
            return getCaseReferencesFromVariables(task, variables);
        }

        return getCaseReferencesFromCsv(externalTask, task.getType());
    }

    private MigrationVariables migrationVariables(ExternalTask externalTask) {
        return new MigrationVariables(
            externalTask.getVariable(CASE_IDS),
            externalTask.getVariable(SCENARIO),
            externalTask.getVariable(DASHBOARD_TASK_ID),
            externalTask.getVariable(DASHBOARD_PROCESS_INSTANCE_ID),
            externalTask.getVariable(DASHBOARD_PROCESS_INSTANCE_IDS),
            externalTask.getVariable(NOTIFICATION_CAMUNDA_PROCESS_IDENTIFIER),
            externalTask.getVariable(CASE_NOTE_ELEMENT_ID),
            externalTask.getVariable(NOTIFY_EVENT_ID)
        );
    }

    private <T extends CaseReference> List<T> getCaseReferencesFromExcel(MigrationTask<T> task, FileValue excelFileValue) {
        byte[] excelBytes = readExcelBytes(excelFileValue);
        if (!ExcelMappable.class.isAssignableFrom(task.getType())) {
            return new ArrayList<>();
        }
        return caseReferenceCsvLoader.loadFromExcelBytes(task.getType(), excelBytes);
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
        MigrationTask<T> task,
        MigrationVariables variables
    ) {
        List<String> caseReferenceIds = Arrays.stream(variables.caseIds().split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
        List<String> dashboardProcessInstanceIdList = toList(variables.dashboardProcessInstanceIds());

        List<T> caseReferences = IntStream.range(0, caseReferenceIds.size())
            .mapToObj(index -> buildCaseReference(
                task.getType(),
                caseReferenceIds.get(index),
                variables.withDashboardProcessInstanceId(
                    resolveDashboardProcessInstanceId(index, variables.dashboardProcessInstanceId(), dashboardProcessInstanceIdList)
                )
            ))
            .toList();
        log.info("Created {} case references from Camunda variables", caseReferences.size());
        return caseReferences;
    }

    private List<String> toList(String values) {
        if (values == null || values.isBlank()) {
            return List.of();
        }
        return Arrays.stream(values.split(","))
            .map(String::trim)
            .toList();
    }

    private String resolveDashboardProcessInstanceId(
        int caseIndex,
        String dashboardProcessInstanceId,
        List<String> dashboardProcessInstanceIds
    ) {
        if (!dashboardProcessInstanceIds.isEmpty()) {
            return caseIndex < dashboardProcessInstanceIds.size()
                ? dashboardProcessInstanceIds.get(caseIndex)
                : null;
        }
        return dashboardProcessInstanceId;
    }

    private <T extends CaseReference> T buildCaseReference(
        Class<T> type,
        String caseId,
        MigrationVariables variables
    ) {
        if (variables.scenario() != null) {
            return type.cast(buildScenarioCaseReference(caseId, variables.scenario()));
        }
        if (variables.dashboardTaskId() != null) {
            return type.cast(buildDashboardNotificationTaskCaseReference(
                caseId,
                variables.dashboardTaskId(),
                variables.dashboardProcessInstanceId()
            ));
        }
        if (variables.camundaProcessIdentifier() != null) {
            return type.cast(buildNotificationCaseReference(caseId, variables.camundaProcessIdentifier()));
        }
        if (variables.caseNoteElementId() != null) {
            return type.cast(buildCaseNoteReference(caseId, variables.caseNoteElementId()));
        }
        if (variables.notifyEventId() != null) {
            return type.cast(buildNotifyRpaFeedCaseReference(caseId, variables.notifyEventId()));
        }
        return type.cast(buildBaseCaseReference(caseId));
    }

    private DashboardNotificationTaskCaseReference buildDashboardNotificationTaskCaseReference(
        String caseId,
        String dashboardTaskId,
        String dashboardProcessInstanceId
    ) {
        DashboardNotificationTaskCaseReference caseReference = new DashboardNotificationTaskCaseReference();
        caseReference.setCaseReference(caseId);
        caseReference.setDashboardTaskId(dashboardTaskId);
        caseReference.setDashboardProcessInstanceId(dashboardProcessInstanceId);
        return caseReference;
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

    private record MigrationVariables(
        String caseIds,
        String scenario,
        String dashboardTaskId,
        String dashboardProcessInstanceId,
        String dashboardProcessInstanceIds,
        String camundaProcessIdentifier,
        String caseNoteElementId,
        String notifyEventId
    ) {

        boolean hasCaseIds() {
            return caseIds != null && !caseIds.isEmpty();
        }

        MigrationVariables withDashboardProcessInstanceId(String processInstanceId) {
            return new MigrationVariables(
                caseIds,
                scenario,
                dashboardTaskId,
                processInstanceId,
                dashboardProcessInstanceIds,
                camundaProcessIdentifier,
                caseNoteElementId,
                notifyEventId
            );
        }
    }
}
