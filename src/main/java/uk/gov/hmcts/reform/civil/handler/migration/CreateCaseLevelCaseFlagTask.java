package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseFlagCaseReference;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CASE_FLAGS;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Component
public class CreateCaseLevelCaseFlagTask extends MigrationTask<CaseFlagCaseReference> {

    private static final String ACTIVE = "Active";
    private static final String OTHER = "Other";

    public CreateCaseLevelCaseFlagTask() {
        super(CaseFlagCaseReference.class);
    }

    @Override
    protected String getEventSummary() {
        return "Create case-level flag via migration task";
    }

    @Override
    protected String getTaskName() {
        return "CreateCaseLevelCaseFlagTask";
    }

    @Override
    protected String getEventDescription() {
        return "This task creates an Other flag at case level";
    }

    @Override
    protected CaseEvent getCaseEvent() {
        return CREATE_CASE_FLAGS;
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, CaseFlagCaseReference caseReference) {
        validate(caseData, caseReference);

        Flags existingFlags = caseData.getCaseFlags();
        List<Element<FlagDetail>> details = existingFlags == null || existingFlags.getDetails() == null
            ? new ArrayList<>()
            : new ArrayList<>(existingFlags.getDetails());
        details.add(element(new FlagDetail()
                                .setName(OTHER)
                                .setFlagComment(caseReference.getComment())
                                .setStatus(ACTIVE)));

        Flags updatedFlags = new Flags()
            .setPartyName(existingFlags == null ? null : existingFlags.getPartyName())
            .setRoleOnCase(existingFlags == null ? null : existingFlags.getRoleOnCase())
            .setDetails(details);
        return caseData.toBuilder().caseFlags(updatedFlags).build();
    }

    private void validate(CaseData caseData, CaseFlagCaseReference caseReference) {
        if (caseData == null) {
            throw new IllegalArgumentException("CaseData must not be null");
        }
        if (caseReference == null || caseReference.getCaseReference() == null) {
            throw new IllegalArgumentException("CaseReference fields must not be null");
        }
        if (caseReference.getComment() == null || caseReference.getComment().isBlank()) {
            throw new IllegalArgumentException("Case flag comment must not be blank");
        }
    }
}
