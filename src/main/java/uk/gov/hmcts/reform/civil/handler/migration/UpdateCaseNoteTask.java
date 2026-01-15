package uk.gov.hmcts.reform.civil.handler.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseNoteReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class UpdateCaseNoteTask extends MigrationTask<CaseNoteReference> {

    public UpdateCaseNoteTask() {
        super(CaseNoteReference.class);
    }

    @Override
    protected String getEventSummary() {
        return "Update case note via migration task";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, CaseNoteReference obj) {
        List<Element<CaseNote>> caseNotes = caseData.getCaseNotes();
        if (caseNotes != null) {
            caseNotes.removeIf(caseNoteElement -> caseNoteElement.getId().equals(UUID.fromString(obj.getCaseNoteItemId())));
            caseData.setCaseNotes(caseNotes);
            log.info("Case note for given caseId {} and given caseNoteItemId {} removed successfully",
                     obj.getCaseReference(), obj.getCaseNoteItemId());
        }
        return caseData;
    }

    @Override
    protected String getEventDescription() {
        return "This task updates case notes on the case";
    }

    @Override
    protected String getTaskName() {
        return "UpdateCaseNoteTask";
    }
}
