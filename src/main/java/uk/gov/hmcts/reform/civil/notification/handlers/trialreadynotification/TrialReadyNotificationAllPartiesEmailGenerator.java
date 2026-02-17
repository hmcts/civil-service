package uk.gov.hmcts.reform.civil.notification.handlers.trialreadynotification;

import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TrialReadyNotificationAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public TrialReadyNotificationAllPartiesEmailGenerator(TrialReadyNotificationAppSolOneEmailDTOGenerator appSolOneEmailGenerator,
                                                  TrialReadyNotificationRespSolOneEmailDTOGenerator respSolOneEmailGenerator,
                                                  TrialReadyNotificationRespSolTwoEmailDTOGenerator respSolTwoEmailGenerator,
                                                  TrialReadyNotificationClaimantEmailDTOGenerator claimantEmailDTOGenerator,
                                                  TrialReadyNotificationDefendantEmailDTOGenerator defendantEmailDTOGenerator
    ) {
        super(List.of(appSolOneEmailGenerator, respSolOneEmailGenerator, respSolTwoEmailGenerator,
                      claimantEmailDTOGenerator, defendantEmailDTOGenerator));
    }
}
