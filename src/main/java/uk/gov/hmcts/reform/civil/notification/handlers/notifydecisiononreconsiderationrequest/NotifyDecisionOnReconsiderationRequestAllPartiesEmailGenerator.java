package uk.gov.hmcts.reform.civil.notification.handlers.notifydecisiononreconsiderationrequest;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class NotifyDecisionOnReconsiderationRequestAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public NotifyDecisionOnReconsiderationRequestAllPartiesEmailGenerator(
        NotifyDecisionOnReconsiderationRequestAppSolEmailDTOGenerator appSolEmailDTOGenerator,
        NotifyDecisionOnReconsiderationRequestRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        NotifyDecisionOnReconsiderationRequestRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator,
        NotifyDecisionOnReconsiderationRequestClaimantEmailDTOGenerator claimantEmailDTOGenerator,
        NotifyDecisionOnReconsiderationRequestDefendantEmailDTOGenerator defendantEmailDTOGenerator
    ) {
        super(List.of(
            appSolEmailDTOGenerator,
            respSolOneEmailDTOGenerator,
            respSolTwoEmailDTOGenerator,
            claimantEmailDTOGenerator,
            defendantEmailDTOGenerator
        ));
    }
}
