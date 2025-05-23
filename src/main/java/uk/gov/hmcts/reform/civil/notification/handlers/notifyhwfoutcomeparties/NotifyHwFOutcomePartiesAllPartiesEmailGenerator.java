package uk.gov.hmcts.reform.civil.notification.handlers.notifyhwfoutcomeparties;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class NotifyHwFOutcomePartiesAllPartiesEmailGenerator extends AllPartiesEmailGenerator {
    public NotifyHwFOutcomePartiesAllPartiesEmailGenerator(
        NotifyHwFOutcomePartiesClaimantEmailDTOGenerator notifyHwFOutcomePartiesClaimantEmailDTOGenerator
    ) {
        super(List.of(notifyHwFOutcomePartiesClaimantEmailDTOGenerator));
    }
}
