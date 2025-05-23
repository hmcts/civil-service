package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaim;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class NotifyClaimAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public NotifyClaimAllPartiesEmailGenerator(NotifyClaimAppSolEmailDTOGenerator notifyClaimAppSolEmailDTOGenerator,
                                               NotifyClaimRespOneSolEmailDTOGenerator notifyClaimRespOneSolEmailDTOGenerator,
                                               NotifyClaimRespSolTwoEmailDTOGenerator notifyClaimRespSolTwoEmailDTOGenerator) {
        super(List.of(notifyClaimAppSolEmailDTOGenerator,
                      notifyClaimRespOneSolEmailDTOGenerator,
                      notifyClaimRespSolTwoEmailDTOGenerator));
    }
}
