package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaimSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isRespondentResponseLangIsBilingual;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL;

@Component
public class ContactDetailsChangeTransitionBuilder extends MidTransitionBuilder {

    public ContactDetailsChangeTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.CONTACT_DETAILS_CHANGE, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(FULL_DEFENCE).onlyWhen(fullDefenceSpec.and(not(isRespondentResponseLangIsBilingual)))
            .moveTo(PART_ADMISSION).onlyWhen(partAdmissionSpec.and(not(isRespondentResponseLangIsBilingual)))
            .moveTo(FULL_ADMISSION).onlyWhen(fullAdmissionSpec.and(not(isRespondentResponseLangIsBilingual)))
            .moveTo(COUNTER_CLAIM).onlyWhen(counterClaimSpec.and(not(isRespondentResponseLangIsBilingual)))
            .moveTo(RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL).onlyWhen(isRespondentResponseLangIsBilingual)
            .set(flags -> {
                flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), true);
            });
    }
}
