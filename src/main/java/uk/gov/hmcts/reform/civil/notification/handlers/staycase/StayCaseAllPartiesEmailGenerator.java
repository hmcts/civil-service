package uk.gov.hmcts.reform.civil.notification.handlers.staycase;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class StayCaseAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public StayCaseAllPartiesEmailGenerator(StayCaseClaimantEmailDTOGenerator claimant,
                                          StayCaseDefendantEmailDTOGenerator defendant,
                                          StayCaseDefendantTwoEmailDTOGenerator defendantTwo) {
        super(List.of(claimant, defendantTwo, defendant));
    }
}
