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
public class RemoveCaseNoteTask extends MigrationTask<CaseNoteReference> {

    public RemoveCaseNoteTask() {
        super(CaseNoteReference.class);
    }

    @Override
    protected String getEventSummary() {
        return "Remove case note via migration task";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, CaseNoteReference obj) {
        List<Element<CaseNote>> caseNotes = caseData.getCaseNotes();
        if (caseNotes != null) {
            caseNotes.removeIf(caseNoteElement -> caseNoteElement.getId().equals(UUID.fromString(obj.getCaseNoteElementId())));
            caseData.setCaseNotes(caseNotes);
            log.info("Case note for given caseId {} and given caseNoteElementId {} removed successfully",
                     obj.getCaseReference(), obj.getCaseNoteElementId());
        }
        return caseData;
    }

    @Override
    protected String getEventDescription() {
        return "This task removes case note on the case";
    }

    @Override
    protected String getTaskName() {
        return "RemoveCaseNoteTask";
    }
}
