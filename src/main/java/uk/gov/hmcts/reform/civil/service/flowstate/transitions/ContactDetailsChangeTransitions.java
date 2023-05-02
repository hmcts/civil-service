package uk.gov.hmcts.reform.civil.service.flowstate.transitions;

import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.grammar.State;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaimSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isRespondentResponseLangIsBilingual;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CONTACT_DETAILS_CHANGE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL;

public class ContactDetailsChangeTransitions implements StateFlowEngineTransitions {
    @Override
    public State<FlowState.Main> defineTransitions(State<FlowState.Main> previousState) {
        return previousState.state(CONTACT_DETAILS_CHANGE)
            .transitionTo(FULL_DEFENCE).onlyIf(fullDefenceSpec.and(not(isRespondentResponseLangIsBilingual)))
            .transitionTo(PART_ADMISSION).onlyIf(partAdmissionSpec.and(not(isRespondentResponseLangIsBilingual)))
            .transitionTo(FULL_ADMISSION).onlyIf(fullAdmissionSpec.and(not(isRespondentResponseLangIsBilingual)))
            .transitionTo(COUNTER_CLAIM).onlyIf(counterClaimSpec.and(not(isRespondentResponseLangIsBilingual)))
            .transitionTo(RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL).onlyIf(isRespondentResponseLangIsBilingual)
                .set(flags -> flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), true));
    }
}
