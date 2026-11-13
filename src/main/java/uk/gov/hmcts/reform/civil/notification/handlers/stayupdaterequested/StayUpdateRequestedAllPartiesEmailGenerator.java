package uk.gov.hmcts.reform.civil.notification.handlers.stayupdaterequested;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class StayUpdateRequestedAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public StayUpdateRequestedAllPartiesEmailGenerator(StayUpdateRequestedClaimantEmailDTOGenerator claimant,
                                          StayUpdateRequestedDefendantEmailDTOGenerator defendant,
                                          StayUpdateRequestedDefendantTwoEmailDTOGenerator defendantTwo) {
        super(List.of(claimant, defendantTwo, defendant));
    }
}
