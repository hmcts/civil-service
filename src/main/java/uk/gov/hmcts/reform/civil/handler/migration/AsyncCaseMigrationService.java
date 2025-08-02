package uk.gov.hmcts.reform.civil.handler.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AsyncCaseMigrationService {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper objectMapper;
    private final int migrationBatchSize;
    private final int migrationWaitTime;

    public AsyncCaseMigrationService(
        CoreCaseDataService coreCaseDataService,
        CaseDetailsConverter caseDetailsConverter,
        ObjectMapper objectMapper,
        @Value("${migration.batchsize:500}") int migrationBatchSize,
        @Value("${migration.wait-time-mins:10}") int migrationWaitTime
    ) {
        this.coreCaseDataService = coreCaseDataService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.objectMapper = objectMapper;
        this.migrationBatchSize = migrationBatchSize;
        this.migrationWaitTime = migrationWaitTime;
    }

    @Async("asyncHandlerExecutor")
    public <T extends CaseReference> void migrateCasesAsync(
        MigrationTask<T> task,
        List<T> caseReferences
    ) {
        int count = 0;
        int batchCount = 1;
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
