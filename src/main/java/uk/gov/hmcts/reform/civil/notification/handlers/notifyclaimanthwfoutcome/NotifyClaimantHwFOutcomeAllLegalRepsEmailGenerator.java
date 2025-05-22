package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimanthwfoutcome;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AllLegalRepsEmailGenerator;

public class NotifyClaimantHwFOutcomeAllLegalRepsEmailGenerator extends AllLegalRepsEmailGenerator {
    public NotifyClaimantHwFOutcomeAllLegalRepsEmailGenerator(
        NotifyClaimantHwFOutcomeAppSolOneEmailDTOGenerator notifyClaimantHwFOutcomeAppSolOneEmailGenerator,
        NotifyClaimantHwFOutcomeRespSolOneEmailDTOGenerator notifyClaimantHwFOutcomeRespSolOneEmailGenerator,
        NotifyClaimantHwFOutcomeRespSolTwoEmailDTOGenerator notifyClaimantHwFOutcomeRespSolTwoEmailGenerator
    ) {
        super(notifyClaimantHwFOutcomeAppSolOneEmailGenerator,
            notifyClaimantHwFOutcomeRespSolOneEmailGenerator,
            notifyClaimantHwFOutcomeRespSolTwoEmailGenerator);
    }

    @Override
    protected boolean shouldNotifyRespondents(CaseData caseData) {
        return caseData.getClaimIssuedHwfDetails() != null
            && caseData.getClaimIssuedHwfDetails().getHwfCaseEvent() != null;
    }
}
