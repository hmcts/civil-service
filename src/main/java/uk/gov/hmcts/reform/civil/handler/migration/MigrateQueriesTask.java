package uk.gov.hmcts.reform.civil.handler.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil;

@Slf4j
@Component
public class MigrateQueriesTask extends MigrationTask<CaseReference> {

    public MigrateQueriesTask() {
        super(CaseReference.class);
    }

    @Override
    protected String getTaskName() {
        return "MigrateQueriesTask";
    }

    @Override
    protected String getEventSummary() {
        return "Migrate queries";
    }

    @Override
    protected String getEventDescription() {
        return "Migrate queries";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, CaseReference caseReference) {
        log.info("Migrating queries for case reference: {}", caseReference.getCaseReference());
        CaseQueriesUtil.migrateAllQueries(caseData);
        return caseData;
    }
}
