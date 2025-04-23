package uk.gov.hmcts.reform.civil.notification.handlers;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Set;

public interface PartiesEmailGenerator {

    Set<EmailDTO> getPartiesToNotify(CaseData caseData);

}
