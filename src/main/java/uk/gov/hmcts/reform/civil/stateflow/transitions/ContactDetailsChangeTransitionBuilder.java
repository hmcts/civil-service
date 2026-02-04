package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LanguagePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ResponsePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ContactDetailsChangeTransitionBuilder extends MidTransitionBuilder {

    public ContactDetailsChangeTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.CONTACT_DETAILS_CHANGE, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(FULL_DEFENCE, transitions)
            .onlyWhen(ResponsePredicate.isType(RespondentResponseTypeSpec.FULL_DEFENCE)
                .and(not(LanguagePredicate.onlyInitialResponseIsBilingual)), transitions)

            .moveTo(PART_ADMISSION, transitions)
            .onlyWhen(ResponsePredicate.isType(RespondentResponseTypeSpec.PART_ADMISSION)
                .and(not(LanguagePredicate.onlyInitialResponseIsBilingual)), transitions)

            .moveTo(FULL_ADMISSION, transitions)
            .onlyWhen(ResponsePredicate.isType(RespondentResponseTypeSpec.FULL_ADMISSION)
                .and(not(LanguagePredicate.onlyInitialResponseIsBilingual)), transitions)

            .moveTo(COUNTER_CLAIM, transitions)
            .onlyWhen(ResponsePredicate.isType(RespondentResponseTypeSpec.COUNTER_CLAIM)
                .and(not(LanguagePredicate.onlyInitialResponseIsBilingual)), transitions)

            .moveTo(RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL, transitions)
            .onlyWhen(LanguagePredicate.onlyInitialResponseIsBilingual, transitions)
            .set(flags ->
                     flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), true), transitions);
    }
}
