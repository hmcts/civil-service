package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.ExcelCaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.bundle.BundleCreationService;

@Component
public class CreateBundleMigrationTask extends MigrationTask<ExcelCaseReference> {

    public CreateBundleMigrationTask(BundleCreationService bundleCreationService) {
        super(ExcelCaseReference.class);
        this.bundleCreationService = bundleCreationService;
    }

    public final BundleCreationService bundleCreationService;

    @Override
    protected String getEventSummary() {
        return "Create Bundle for cases via migration task";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, ExcelCaseReference caseReference) {
        if (caseData == null || caseReference == null) {
            throw new IllegalArgumentException("CaseData and CaseReference must not be null");
        }

        // Perform the migration
        bundleCreationService.createBundle(Long.valueOf(caseReference.getCaseReference()));
        return caseData;
    }

    @Override
    protected String getEventDescription() {
        return "This task creates the bundle for cases based on the provided case references.";
    }

    @Override
    protected String getTaskName() {
        return "CreateBundleMigrationTask";
    }
}
