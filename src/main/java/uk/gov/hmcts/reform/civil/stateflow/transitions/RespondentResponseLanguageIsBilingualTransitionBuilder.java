package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LipPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ResponsePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RespondentResponseLanguageIsBilingualTransitionBuilder extends MidTransitionBuilder {

    public RespondentResponseLanguageIsBilingualTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(FULL_DEFENCE, transitions)
            .onlyWhen(ResponsePredicate.isType(RespondentResponseTypeSpec.FULL_DEFENCE)
                .and(LipPredicate.isTranslatedDocumentUploaded), transitions)

            .moveTo(PART_ADMISSION, transitions)
            .onlyWhen(ResponsePredicate.isType(RespondentResponseTypeSpec.PART_ADMISSION)
                .and(LipPredicate.isTranslatedDocumentUploaded), transitions)

            .moveTo(FULL_ADMISSION, transitions)
            .onlyWhen(ResponsePredicate.isType(RespondentResponseTypeSpec.FULL_ADMISSION)
                .and(LipPredicate.isTranslatedDocumentUploaded), transitions)

            .moveTo(COUNTER_CLAIM, transitions)
            .onlyWhen(ResponsePredicate.isType(RespondentResponseTypeSpec.COUNTER_CLAIM)
                .and(LipPredicate.isTranslatedDocumentUploaded), transitions);
    }

}
