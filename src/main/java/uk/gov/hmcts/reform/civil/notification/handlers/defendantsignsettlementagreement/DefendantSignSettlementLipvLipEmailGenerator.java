package uk.gov.hmcts.reform.civil.notification.handlers.defendantsignsettlementagreement;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.LipvLipEmailGenerator;

@Component
public class DefendantSignSettlementLipvLipEmailGenerator extends LipvLipEmailGenerator {
    public DefendantSignSettlementLipvLipEmailGenerator(DefendantSignSettlementClaimantEmailDTOGenerator claimantEmailDTOGenerator, DefendantSignSettlementDefendantEmailDTOGenerator defendantEmailDTOGenerator) {
        super(claimantEmailDTOGenerator, defendantEmailDTOGenerator);
    }
}
