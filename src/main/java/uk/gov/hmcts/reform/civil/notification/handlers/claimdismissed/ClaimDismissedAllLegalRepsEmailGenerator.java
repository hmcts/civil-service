package uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AllLegalRepsEmailGenerator;

@Component
@Slf4j
public class ClaimDismissedAllLegalRepsEmailGenerator extends AllLegalRepsEmailGenerator {

    public ClaimDismissedAllLegalRepsEmailGenerator(
        ClaimDismissedAppSolOneEmailDTOGenerator claimDismissedAppSolOneEmailGenerator,
        ClaimDismissedRespSolOneEmailDTOGenerator claimDismissedRespSolOneEmailGenerator,
        ClaimDismissedRespSolTwoEmailDTOGenerator claimDismissedRespSolTwoEmailGenerator
    ) {
        super(claimDismissedAppSolOneEmailGenerator,
            claimDismissedRespSolOneEmailGenerator,
            claimDismissedRespSolTwoEmailGenerator);
    }

    @Override
    protected boolean shouldNotifyRespondents(CaseData caseData) {
        return caseData.getClaimDismissedDate() != null;
    }
}
