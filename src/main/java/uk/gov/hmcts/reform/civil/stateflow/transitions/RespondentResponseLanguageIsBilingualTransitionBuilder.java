package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.isTranslatedDocumentUploaded;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaimSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;

@Component
public class RespondentResponseLanguageIsBilingualTransitionBuilder extends MidTransitionBuilder {

    public RespondentResponseLanguageIsBilingualTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(FULL_DEFENCE).onlyWhen(fullDefenceSpec.and(isTranslatedDocumentUploaded))
            .moveTo(PART_ADMISSION).onlyWhen(partAdmissionSpec.and(isTranslatedDocumentUploaded))
            .moveTo(FULL_ADMISSION).onlyWhen(fullAdmissionSpec.and(isTranslatedDocumentUploaded))
            .moveTo(COUNTER_CLAIM).onlyWhen(counterClaimSpec.and(isTranslatedDocumentUploaded));
    }

}
