package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.UpdateTTLCaseReference;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.cmc.model.TTL;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class UpdateSystemTTLTask extends MigrationTask<UpdateTTLCaseReference> {

    private static final String TTL_FIELD = "TTL";
    private static final int RETENTION_PERIOD_YEARS = 6;

    public UpdateSystemTTLTask() {
        super(UpdateTTLCaseReference.class);
    }

    @Override
    protected String getEventDescription() {
        return "This task updates system TTL on the case";
    }

    @Override
    protected String getEventSummary() {
        return "Update case system TTL via migration task";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, UpdateTTLCaseReference caseReference) {
        if (caseData == null || caseReference == null || caseReference.getCaseReference() == null) {
            throw new IllegalArgumentException("CaseData and CaseReference fields must not be null");
        }
        return caseData;
    }

    @Override
    protected Map<String, Object> migrateCmcCaseData(
        CaseDetails caseDetails,
        UpdateTTLCaseReference caseReference
    ) {
        if (caseDetails == null || caseDetails.getData() == null
            || caseReference == null || caseReference.getCaseReference() == null) {
            throw new IllegalArgumentException("CaseData and CaseReference fields must not be null");
        }

        Map<String, Object> updatedCaseData = new HashMap<>(caseDetails.getData());
        if (caseReference.getSystemTTL() != null) {
            return updatedCaseData;
        }

        LocalDateTime lastModified = caseDetails.getLastModified();
        if (lastModified == null) {
            throw new IllegalArgumentException("Last modified date must not be null when system TTL is missing");
        }

        TTL ttl = new TTL();
        ttl.setSystemTTL(lastModified.toLocalDate().plusYears(RETENTION_PERIOD_YEARS));
        ttl.setSuspended(YesOrNo.NO.getLabel());
        updatedCaseData.put(TTL_FIELD, ttl);
        return updatedCaseData;
    }

    @Override
    protected String getTaskName() {
        return "UpdateSystemTTLTask";
    }
}
