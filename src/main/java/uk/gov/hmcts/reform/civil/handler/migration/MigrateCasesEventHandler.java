package uk.gov.hmcts.reform.civil.handler.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MigrateCasesEventHandler extends BaseExternalTaskHandler {

    private final CaseReferenceCsvLoader caseReferenceCsvLoader;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final MigrationTaskFactory migrationTaskFactory;
    private final ObjectMapper objectMapper;

    @Value("${migration.batchsize:500}")
    private final int migrationBatchSize;
    @Value("${migration.wait-time-mins:10}")
    private final int migrationWaitTime;

    @Value("${migration.csvFile.decrypt.key:DUMMY_KEY}")
    private final String encryptionSecret;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        assert externalTask.getVariable("taskName") != null;
        if (externalTask.getVariable("caseIds") == null
            && externalTask.getVariable("csvFileName") == null) {
            throw new AssertionError("caseIds or csvFileName is null");
        }
        String taskName = externalTask.getVariable("taskName");
        Optional<MigrationTask> migrationTask = migrationTaskFactory.getMigrationTask(taskName);
        migrationTask.orElseThrow(() -> new IllegalArgumentException("No migration task found for: " + taskName));
        log.info("Starting migration task: {}", taskName);
        MigrationTask task = migrationTask.get();
        String caseIds = externalTask.getVariable("caseIds");
        String csvFileName = externalTask.getVariable("csvFileName");
        List<CaseReference> caseReferences = getCaseReferenceList(caseIds, csvFileName);

        log.info("Found {} case references to process", caseReferences.size());
        if (caseReferences.isEmpty()) {
            log.warn("No case references found to process");
            return ExternalTaskData.builder().build();
        }
        for (CaseReference caseReference : caseReferences) {
            try {
                log.info("Migrating case with ID: {}", caseReference);
                StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseReference.getCaseReference(), CaseEvent.UPDATE_CASE_DATA);
                CaseData caseData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());
                caseData = task.migrateCaseData(caseData);
                CaseDataContent caseDataContent = buildCaseDataContent(startEventResponse, caseData, task);
                coreCaseDataService.submitUpdate(
                    caseReference.getCaseReference(),
                    caseDataContent
                );
                log.info("Migration completed for case ID: {}", caseReference.getCaseReference());
            } catch (Exception e) {
                log.error("Error migrating case with ID: {}. Error: {}", caseReference.getCaseReference(), e.getMessage(), e);
            }
        }
        return ExternalTaskData.builder().build();
    }

    protected List<CaseReference> getCaseReferenceList(String caseIds, String csvFileName) {
        List<CaseReference> caseReferences;
        if (caseIds == null || caseIds.isEmpty()) {
            log.info("Loading case references from CSV file: {}", csvFileName);
            if (CaseMigrationEncryptionUtil.isFileEncrypted(csvFileName)) {
                caseReferences = caseReferenceCsvLoader.loadCaseReferenceList(csvFileName, encryptionSecret);
            } else {
                caseReferences = caseReferenceCsvLoader.loadCaseReferenceList(csvFileName);
            }
        } else {
            log.info("Using provided case IDs: {}", caseIds);
            caseReferences = caseIds.isEmpty()
                ? List.of()
                : List.of(caseIds.split(",")).stream()
                .map(String::trim)
                .map(CaseReference::new)
                .toList();
        }
        return caseReferences;
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
