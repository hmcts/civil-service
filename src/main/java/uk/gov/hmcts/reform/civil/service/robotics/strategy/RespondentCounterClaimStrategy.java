package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsRespondentResponseSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ResponseExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1v2SameSolicitorSameResponse;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2ResponseExists;

@Slf4j
@Component
@RequiredArgsConstructor
public class RespondentCounterClaimStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsRespondentResponseSupport respondentResponseSupport;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null;
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        log.info("Building respondent counter claim robotics events for caseId {}", caseData.getCcdCaseReference());

        if (defendant1ResponseExists.test(caseData)) {
            respondentResponseSupport.addRespondentMiscEvent(
                builder,
                sequenceGenerator,
                caseData,
                caseData.getRespondent1(),
                true,
                caseData.getRespondent1ResponseDate()
            );

            if (defendant1v2SameSolicitorSameResponse.test(caseData)) {
                LocalDateTime respondent2Date = respondentResponseSupport.resolveRespondent2ActualOrFallbackDate(caseData);
                respondentResponseSupport.addRespondentMiscEvent(
                    builder,
                    sequenceGenerator,
                    caseData,
                    caseData.getRespondent2(),
                    false,
                    respondent2Date
                );
                return;
            }
        }

        if (defendant2ResponseExists.test(caseData)) {
            respondentResponseSupport.addRespondentMiscEvent(
                builder,
                sequenceGenerator,
                caseData,
                caseData.getRespondent2(),
                false,
                caseData.getRespondent2ResponseDate()
            );
        }
    }

    // Response type validation is handled by state transitions; inconsistent data should behave like master.
}
