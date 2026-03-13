package uk.gov.hmcts.reform.civil.notification.handlers.recordjudgementnotification;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class RecordJudgementNotificationAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public RecordJudgementNotificationAllPartiesEmailGenerator(
        RecordJudgementNotificationAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
        RecordJudgementNotificationRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        RecordJudgementNotificationRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator,
        RecordJudgementNotificationClaimantEmailDTOGenerator claimantEmailDTOGenerator,
        RecordJudgementNotificationDefendantEmailDTOGenerator defendantEmailDTOGenerator) {
        super(List.of(appSolOneEmailDTOGenerator,
                      respSolOneEmailDTOGenerator,
                      respSolTwoEmailDTOGenerator,
                      claimantEmailDTOGenerator,
                      defendantEmailDTOGenerator));
    }
}
