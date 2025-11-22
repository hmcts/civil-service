package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildMiscEvent;

@Component
@RequiredArgsConstructor
public class RespondentLitigationFriendStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
            && (caseData.getRespondent1LitigationFriendCreatedDate() != null
            || caseData.getRespondent2LitigationFriendCreatedDate() != null);
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        if (caseData.getRespondent1LitigationFriendCreatedDate() != null) {
            builder.miscellaneous(buildMiscEvent(
                builder,
                sequenceGenerator,
                "Litigation friend added for respondent: " + caseData.getRespondent1().getPartyName(),
                caseData.getRespondent1LitigationFriendCreatedDate()
            ));
        }
        if (caseData.getRespondent2LitigationFriendCreatedDate() != null) {
            builder.miscellaneous(buildMiscEvent(
                builder,
                sequenceGenerator,
                "Litigation friend added for respondent: " + caseData.getRespondent2().getPartyName(),
                caseData.getRespondent2LitigationFriendCreatedDate()
            ));
        }
    }
}
