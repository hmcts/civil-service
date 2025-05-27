package uk.gov.hmcts.reform.civil.notification.handlers.defendantsignsettlementagreement;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class DefendantSignSettlementPartiesEmailGenerator extends AllPartiesEmailGenerator {
    public DefendantSignSettlementPartiesEmailGenerator(DefendantSignSettlementClaimantEmailDTOGenerator claimantEmailDTOGenerator, DefendantSignSettlementDefendantEmailDTOGenerator defendantEmailDTOGenerator) {
        super(List.of(claimantEmailDTOGenerator, defendantEmailDTOGenerator));
    }
}
