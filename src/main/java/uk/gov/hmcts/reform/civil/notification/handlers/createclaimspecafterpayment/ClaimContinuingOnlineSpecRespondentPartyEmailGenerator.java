package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class ClaimContinuingOnlineSpecRespondentPartyEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimContinuingOnlineSpecRespondentPartyEmailGenerator(
            ClaimContinuingOnlineSpecRespondentPartyEmailDTOGenerator dtoGenerator
    ) {
        super(List.of(dtoGenerator));
    }
}
