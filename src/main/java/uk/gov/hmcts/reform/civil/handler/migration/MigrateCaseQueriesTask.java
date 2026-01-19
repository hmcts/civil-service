package uk.gov.hmcts.reform.civil.handler.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil;

@Slf4j
@Component
public class MigrateCaseQueriesTask extends MigrationTask<CaseReference> {

    public MigrateCaseQueriesTask() {
        super(CaseReference.class);
    }

    @Override
    protected String getTaskName() {
        return "MigrateCaseQueriesTask";
    }

    @Override
    protected String getEventSummary() {
        return "Migrate case queries via migration task";
    }

    @Override
    protected String getEventDescription() {
        return "This task migrates case queries on the case";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, CaseReference caseReference) {
        log.info("Migrating case queries for case reference: {}", caseReference.getCaseReference());
        CaseQueriesUtil.migrateAllQueries(caseData);
        CaseQueriesUtil.clearOldQueryCollections(caseData);
        return caseData;
    }
}
