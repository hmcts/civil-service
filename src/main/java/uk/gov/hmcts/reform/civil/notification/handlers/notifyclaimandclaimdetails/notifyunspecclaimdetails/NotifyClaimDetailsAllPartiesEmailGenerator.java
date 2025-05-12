package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaimdetails;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

@Component
public class NotifyClaimDetailsAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public NotifyClaimDetailsAllPartiesEmailGenerator(NotifyClaimDetailsAppSolEmailDTOGenerator notifyClaimDetailsAppSolEmailDTOGenerator,
                                                      NotifyClaimDetailsRespOneSolEmailDTOGenerator notifyClaimDetailsRespOneSolEmailDTOGenerator,
                                                      NotifyClaimDetailsRespSolTwoEmailDTOGenerator notifyClaimDetailsRespSolTwoEmailDTOGenerator) {
        super(notifyClaimDetailsAppSolEmailDTOGenerator,
              notifyClaimDetailsRespOneSolEmailDTOGenerator,
              notifyClaimDetailsRespSolTwoEmailDTOGenerator,
              null,
              null);
    }
}
