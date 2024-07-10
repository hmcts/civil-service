package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimIssued;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.specClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineBySystem;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT;

@Component
public class PendingClaimIssuedUnrepresentedDefendantTransitionBuilder extends MidTransitionBuilder {

    public PendingClaimIssuedUnrepresentedDefendantTransitionBuilder(
        FeatureToggleService featureToggleService) {
        super(FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(CLAIM_ISSUED).onlyWhen(claimIssued
                .and(not(specClaim))
                .and(certificateOfServiceEnabled))
            .moveTo(TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT).onlyWhen(takenOfflineBySystem
                .and(specClaim));
    }

    public static final Predicate<CaseData> certificateOfServiceEnabled = caseData ->
        (YES.equals(caseData.getDefendant1LIPAtClaimIssued()) || YES.equals(caseData.getDefendant2LIPAtClaimIssued()));
}
