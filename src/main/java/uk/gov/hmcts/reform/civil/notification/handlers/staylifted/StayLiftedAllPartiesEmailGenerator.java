package uk.gov.hmcts.reform.civil.notification.handlers.staylifted;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class StayLiftedAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public StayLiftedAllPartiesEmailGenerator(StayLiftedClaimantEmailDTOGenerator claimant,
                                          StayLiftedDefendantEmailDTOGenerator defendant,
                                          StayLiftedDefendantTwoEmailDTOGenerator defendantTwo) {
        super(List.of(claimant, defendantTwo, defendant));
    }
}
