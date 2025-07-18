package uk.gov.hmcts.reform.civil.handler.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReferenceCsvLoader;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.utils.CaseMigrationEncryptionUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class MigrateCasesEventHandler extends BaseExternalTaskHandler {

    protected static final String TASK_NAME = "taskName";
    protected static final String CSV_FILE_NAME = "csvFileName";
    private final CaseReferenceCsvLoader caseReferenceCsvLoader;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final MigrationTaskFactory migrationTaskFactory;
    private final ObjectMapper objectMapper;
    private final int migrationBatchSize;
    private final int migrationWaitTime;
    private final String encryptionSecret;

    public MigrateCasesEventHandler(
        CaseReferenceCsvLoader caseReferenceCsvLoader,
        CoreCaseDataService coreCaseDataService,
        CaseDetailsConverter caseDetailsConverter,
        MigrationTaskFactory migrationTaskFactory,
        ObjectMapper objectMapper,
        @Value("${migration.batchsize:500}") int migrationBatchSize,
        @Value("${migration.wait-time-mins:10}") int migrationWaitTime,
        @Value("${migration.csvFile.decrypt.key:DUMMY_KEY}") String encryptionSecret
    ) {
        this.caseReferenceCsvLoader = caseReferenceCsvLoader;
        this.coreCaseDataService = coreCaseDataService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.migrationTaskFactory = migrationTaskFactory;
        this.objectMapper = objectMapper;
        this.migrationBatchSize = migrationBatchSize;
        this.migrationWaitTime = migrationWaitTime;
        this.encryptionSecret = encryptionSecret;
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

        int count = 0;
        int batchCount = 1;
        log.info("Found {} case references to process", caseReferences.size());
        if (caseReferences.isEmpty()) {
            log.warn("No case references found to process");
            return ExternalTaskData.builder().build();
        }

        for (T caseReference : caseReferences) {
            count++;
            try {
                RequestContextHolder.setRequestAttributes(new CustomRequestScopeAttr());
                if (count == migrationBatchSize) {
                    log.info("Batch {} limit reached {}, pausing for {} minutes", batchCount, migrationBatchSize, migrationWaitTime);
                    TimeUnit.MINUTES.sleep(migrationWaitTime);
                    count = 0;
                    batchCount++;
                }
                log.info("Migrating case with ID: {}", caseReference);
                StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseReference.getCaseReference(), CaseEvent.UPDATE_CASE_DATA);
                CaseData caseData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());
                caseData = task.migrateCaseData(caseData, caseReference);
                CaseDataContent caseDataContent = buildCaseDataContent(startEventResponse, caseData, task);
                coreCaseDataService.submitUpdate(caseReference.getCaseReference(), caseDataContent);
                log.info("Migration completed for case ID: {}", caseReference.getCaseReference());
            } catch (RuntimeException e) {
                log.error("Error migrating case with ID: {}. Error: {}", caseReference.getCaseReference(), e.getMessage(), e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        }

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

    protected CaseDataContent buildCaseDataContent(StartEventResponse startEventResponse, CaseData caseData, MigrationTask task) {

        Map<String, Object> updatedData = caseData.toMap(objectMapper);
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId())
                .summary(task.getEventSummary())
                .description(task.getEventDescription())
                .build())
            .data(updatedData)
            .build();
    }
}
