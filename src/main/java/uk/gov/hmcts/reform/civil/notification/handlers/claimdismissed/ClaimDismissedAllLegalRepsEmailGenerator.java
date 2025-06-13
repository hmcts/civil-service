package uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
@Slf4j
public class ClaimDismissedAllLegalRepsEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimDismissedAllLegalRepsEmailGenerator(
        ClaimDismissedAppSolOneEmailDTOGenerator claimDismissedAppSolOneEmailGenerator,
        ClaimDismissedRespSolOneEmailDTOGenerator claimDismissedRespSolOneEmailGenerator,
        ClaimDismissedRespSolTwoEmailDTOGenerator claimDismissedRespSolTwoEmailGenerator
    ) {
        super(List.of(claimDismissedAppSolOneEmailGenerator,
            claimDismissedRespSolOneEmailGenerator,
            claimDismissedRespSolTwoEmailGenerator));
    }

}
