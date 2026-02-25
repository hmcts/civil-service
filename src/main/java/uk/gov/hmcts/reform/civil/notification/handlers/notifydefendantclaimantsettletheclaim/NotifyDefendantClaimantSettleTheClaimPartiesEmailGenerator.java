package uk.gov.hmcts.reform.civil.notification.handlers.notifydefendantclaimantsettletheclaim;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class NotifyDefendantClaimantSettleTheClaimPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public NotifyDefendantClaimantSettleTheClaimPartiesEmailGenerator(
        NotifyDefendantClaimantSettleTheClaimDefendantEmailDTOGenerator defendantEmailDTOGenerator,
        NotifyDefendantClaimantSettleTheClaimRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator) {
        super(List.of(defendantEmailDTOGenerator, respSolOneEmailDTOGenerator));
    }
}
