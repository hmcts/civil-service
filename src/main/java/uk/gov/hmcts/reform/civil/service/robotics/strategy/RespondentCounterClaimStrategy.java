package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsRespondentResponseSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildMiscEvent;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ResponseExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1v2SameSolicitorSameResponse;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2ResponseExists;

@Component
@Order(47)
@RequiredArgsConstructor
public class RespondentCounterClaimStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsRespondentResponseSupport respondentResponseSupport;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
            && hasCounterClaimState(caseData)
            && (defendant1ResponseExists.test(caseData) || defendant2ResponseExists.test(caseData));
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        LocalDateTime resolvedRespondent2Date = respondentResponseSupport.resolveRespondent2ResponseDate(caseData);
        LocalDateTime respondent2ResponseDate = caseData.getRespondent2ResponseDate() != null
            ? caseData.getRespondent2ResponseDate()
            : resolvedRespondent2Date;

        if (defendant1ResponseExists.test(caseData)) {
            LocalDateTime respondent1ResponseDate = caseData.getRespondent1ResponseDate();
            addMiscellaneous(
                builder,
                respondent1ResponseDate,
                respondentResponseSupport.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true)
            );

            if (defendant1v2SameSolicitorSameResponse.test(caseData)) {
                addMiscellaneous(
                    builder,
                    respondent2ResponseDate,
                    respondentResponseSupport.prepareRespondentResponseText(caseData, caseData.getRespondent2(), false)
                );
            }
        }

        if (defendant2ResponseExists.test(caseData)) {
            addMiscellaneous(
                builder,
                respondent2ResponseDate,
                respondentResponseSupport.prepareRespondentResponseText(caseData, caseData.getRespondent2(), false)
            );
        }
    }

    private void addMiscellaneous(EventHistory.EventHistoryBuilder builder, LocalDateTime date, String message) {
        builder.miscellaneous(buildMiscEvent(builder, sequenceGenerator, message, date));
    }

    private boolean hasCounterClaimState(CaseData caseData) {
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return stateFlow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(FlowState.Main.COUNTER_CLAIM.fullName()::equals);
    }
}
