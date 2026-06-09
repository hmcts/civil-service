package uk.gov.hmcts.reform.civil.handler.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.HousingDisrepair;

@Slf4j
@Component
public class MigrateFastTrackHousingDisrepairTask extends MigrationTask<CaseReference> {

    public MigrateFastTrackHousingDisrepairTask() {
        super(CaseReference.class);
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, CaseReference caseReference) {
        log.info("Migrating new fastTrack housing disrepair for case reference: {}", caseReference.getCaseReference());

        HousingDisrepair fastTrackHousingDisrepair = caseData.getFastTrackHousingDisrepair();

        HousingDisrepair fastTrackHousingDisrepairNew = new HousingDisrepair();

        fastTrackHousingDisrepairNew.setClauseA(fastTrackHousingDisrepair.getInput1());
        fastTrackHousingDisrepairNew.setClauseB(fastTrackHousingDisrepair.getInput2());
        fastTrackHousingDisrepairNew.setClauseD(fastTrackHousingDisrepair.getInput3());
        fastTrackHousingDisrepairNew.setClauseE(fastTrackHousingDisrepair.getInput4());
        fastTrackHousingDisrepairNew.setFirstReportDateBy(fastTrackHousingDisrepair.getDate1());
        fastTrackHousingDisrepairNew.setJointStatementDateBy(fastTrackHousingDisrepair.getDate2());

        caseData.setFastTrackHousingDisrepair(fastTrackHousingDisrepairNew);
        return caseData;
    }

    @Override
    protected String getTaskName() {
        return "MigrateFastTrackHousingDisrepairTask";
    }

    @Override
    protected String getEventSummary() {
        return "Migrate to new fastTrack housing disrepair via migration task";
    }

    @Override
    protected String getEventDescription() {
        return "This task migrates new fastTrack housing disrepair on the case";
    }
}
