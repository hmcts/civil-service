package uk.gov.hmcts.reform.civil.handler.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHousingDisrepair;

@Slf4j
@Component
public class MigrateTrialHousingDisrepairTask extends MigrationTask<CaseReference> {

    public MigrateTrialHousingDisrepairTask() {
        super(CaseReference.class);
    }

    @Override
    protected String getTaskName() {
        return "MigrateTrialHousingDisrepairTask";
    }

    @Override
    protected String getEventSummary() {
        return "Migrate to trail housing disrepair via migration task";
    }

    @Override
    protected String getEventDescription() {
        return "This task migrates trail housing disrepair on the case";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, CaseReference caseReference) {
        log.info("Migrating to new trail housing disrepair for case reference: {}", caseReference.getCaseReference());

        TrialHousingDisrepair trialHousingDisrepair = caseData.getTrialHousingDisrepair();

        TrialHousingDisrepair trialHousingDisrepairNew = new TrialHousingDisrepair();

        trialHousingDisrepairNew.setClauseA(trialHousingDisrepair.getInput1());
        trialHousingDisrepairNew.setClauseB(trialHousingDisrepair.getInput2());
        trialHousingDisrepairNew.setClauseD(trialHousingDisrepair.getInput3());
        trialHousingDisrepairNew.setClauseE(trialHousingDisrepair.getInput4());
        trialHousingDisrepairNew.setFirstReportDateBy(trialHousingDisrepair.getDate1());
        trialHousingDisrepairNew.setJointStatementDateBy(trialHousingDisrepair.getDate2());

        caseData.setTrialHousingDisrepair(trialHousingDisrepairNew);
        return caseData;
    }
}
