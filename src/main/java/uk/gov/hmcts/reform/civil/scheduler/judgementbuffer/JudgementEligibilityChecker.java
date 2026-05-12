package uk.gov.hmcts.reform.civil.scheduler.judgementbuffer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.Time;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class JudgementEligibilityChecker {

    private final Time time;

    public boolean isEligibleForJudgement(CaseData caseData) {
        return !isDefenceSubmitted(caseData)
            && !isCaseOffline(caseData)
            && isDeadlinePast(caseData);
    }

    private boolean isDefenceSubmitted(CaseData caseData) {
        return nonNull(caseData.getRespondent1ResponseDate());
    }

    private boolean isCaseOffline(CaseData caseData) {
        return nonNull(caseData.getTakenOfflineDate());
    }

    private boolean isDeadlinePast(CaseData caseData) {
        return nonNull(caseData.getRespondent1ResponseDeadline())
            && caseData.getRespondent1ResponseDeadline().isBefore(time.now());
    }
}
