package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE;

@Component
public class PastApplicantResponseDeadlineAwaitingCamundaTransitionBuilder extends MidTransitionBuilder {

    public PastApplicantResponseDeadlineAwaitingCamundaTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE)
            .onlyWhen(applicantOutOfTimeProcessedByCamunda);
    }

    public static final Predicate<CaseData> applicantOutOfTimeProcessedByCamunda = caseData ->
        caseData.getTakenOfflineDate() != null;
}
