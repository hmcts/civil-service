package uk.gov.hmcts.reform.civil.service.flowstate.transitions;

import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.grammar.State;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.contactDetailsChange;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaimSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL;

public class RespondentResponseLanguageIsBilingualTransitions implements StateFlowEngineTransitions {
    @Override
    public State<FlowState.Main> defineTransitions(State<FlowState.Main> previousState) {
        return previousState.state(RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL)
            .transitionTo(FULL_DEFENCE).onlyIf(fullDefenceSpec.and(not(contactDetailsChange)))
            .transitionTo(PART_ADMISSION).onlyIf(partAdmissionSpec.and(not(contactDetailsChange)))
            .transitionTo(FULL_ADMISSION).onlyIf(fullAdmissionSpec.and(not(contactDetailsChange)))
            .transitionTo(COUNTER_CLAIM).onlyIf(counterClaimSpec.and(not(contactDetailsChange)));
    }
}
