package uk.gov.hmcts.reform.civil.notification.handlers.notifydefendantclaimantsettletheclaim;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class NotifyDefendantClaimantSettleTheClaimAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public NotifyDefendantClaimantSettleTheClaimAllPartiesEmailGenerator(
        NotifyDefendantClaimantSettleTheClaimDefendantEmailDTOGenerator defendantEmailDTOGenerator,
        NotifyDefendantClaimantSettleTheClaimRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator) {
        super(List.of(defendantEmailDTOGenerator, respSolOneEmailDTOGenerator));
    }
}
