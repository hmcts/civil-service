package uk.gov.hmcts.reform.civil.stateflow.transitions;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LanguagePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.MoveToNext;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.OnlyWhenNext;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SetNext;
import uk.gov.hmcts.reform.civil.utils.JudgmentAdmissionUtils;
import uk.gov.hmcts.reform.civil.utils.JudicialReferralUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

@RequiredArgsConstructor
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public abstract class TransitionBuilder implements MoveToNext<FlowState.Main> {

    protected final FlowState.Main fromState;
    protected final FeatureToggleService featureToggleService;
    public static final String FLOW_NAME = FlowState.Main.FLOW_NAME;

    @Override
    public MoveToNext<FlowState.Main> moveTo(FlowState.Main toState, List<Transition> transitions) {
        transitions.add(new Transition(fullyQualified(fromState), fullyQualified(toState)));
        return this;
    }

    @Override
    public OnlyWhenNext<FlowState.Main> onlyWhen(Predicate<CaseData> condition, List<Transition> transitions) {
        getCurrentTransition(transitions).setCondition(condition);
        return this;
    }

    @Override
    public SetNext<FlowState.Main> set(Consumer<Map<String, Boolean>> flags, List<Transition> transitions) {
        getCurrentTransition(transitions).setFlags(flags);
        return this;
    }

    @Override
    public SetNext<FlowState.Main> set(BiConsumer<CaseData, Map<String, Boolean>> flags, List<Transition> transitions) {
        getCurrentTransition(transitions).setDynamicFlags(flags);
        return this;
    }

    private Transition getCurrentTransition(List<Transition> transitions) {
        if (transitions.isEmpty()) {
            throw new IllegalStateException("No transition has been defined. Call moveTo before onlyWhen or set.");
        }
        return transitions.get(transitions.size() - 1);
    }

    private String fullyQualified(FlowState.Main state) {
        return FLOW_NAME + "." + state;
    }

    protected Predicate<CaseData> defendantNoCOnlineForCase() {
        return featureToggleService::isDefendantNoCOnlineForCase;
    }

    protected void setRespondentResponseLanguageFlag(CaseData caseData, Map<String, Boolean> flags) {
        flags.put(
            FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(),
            LanguagePredicate.respondentIsBilingual.test(caseData)
        );
    }

    protected void setJudicialReferralFlags(CaseData caseData, Map<String, Boolean> flags) {
        boolean mintiEnabled = featureToggleService.isMultiOrIntermediateTrackEnabled(caseData);
        flags.put(FlowFlag.MINTI_ENABLED.name(), mintiEnabled);
        flags.put(
            FlowFlag.SDO_ENABLED.name(),
            JudicialReferralUtils.shouldMoveToJudicialReferral(caseData, mintiEnabled)
        );
    }

    protected void setLipJudgmentAdmissionFlag(CaseData caseData, Map<String, Boolean> flags) {
        flags.put(
            FlowFlag.LIP_JUDGMENT_ADMISSION.name(),
            JudgmentAdmissionUtils.getLIPJudgmentAdmission(caseData)
        );
    }

    @Override
    public List<Transition> buildTransitions() {
        List<Transition> transitions = new ArrayList<>();
        setUpTransitions(transitions);
        return transitions;
    }

    abstract void setUpTransitions(List<Transition> transitions);
}
