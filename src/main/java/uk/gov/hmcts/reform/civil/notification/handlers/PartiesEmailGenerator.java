package uk.gov.hmcts.reform.civil.notification.handlers;

import java.util.Set;

public interface PartiesEmailGenerator<T extends NotifiableCaseData> {

    Set<EmailDTO> getPartiesToNotify(T caseData, String taskId);
}
