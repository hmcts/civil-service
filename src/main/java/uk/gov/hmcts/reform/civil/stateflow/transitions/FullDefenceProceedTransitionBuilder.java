package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import java.time.LocalDate;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedPastHearingFeeDue;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isInHearingReadiness;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterSDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineSDONotDrawn;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_SDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN;

@Component
public class FullDefenceProceedTransitionBuilder extends MidTransitionBuilder {

    public FullDefenceProceedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.FULL_DEFENCE_PROCEED, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(IN_HEARING_READINESS).onlyWhen(isInHearingReadiness)
            .moveTo(CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE).onlyWhen(caseDismissedPastHearingFeeDue)
            .moveTo(TAKEN_OFFLINE_BY_STAFF).onlyWhen((takenOfflineByStaffAfterClaimantResponseBeforeSDO
                .or(takenOfflineByStaffAfterSDO)
                .or(takenOfflineAfterNotSuitableForSdo))
                .and(not(caseDismissedPastHearingFeeDue)))
            .moveTo(TAKEN_OFFLINE_AFTER_SDO).onlyWhen(takenOfflineAfterSDO)
            .moveTo(TAKEN_OFFLINE_SDO_NOT_DRAWN).onlyWhen(takenOfflineSDONotDrawn)
            .moveTo(IN_MEDIATION).onlyWhen(specSmallClaimCarm);
    }

    public static final Predicate<CaseData> specSmallClaimCarm = caseData ->
        isSpecSmallClaim(caseData) && getCarmEnabledForDate(caseData);

    private static boolean isSpecSmallClaim(CaseData caseData) {
        return SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && SMALL_CLAIM.name().equals(caseData.getResponseClaimTrack());
    }

    private static boolean getCarmEnabledForDate(CaseData caseData) {
        // Date of go live is 1st August, as we use "isAfter" we compare with 31st July
        return caseData.getSubmittedDate().toLocalDate().isAfter(LocalDate.of(2024, 7, 31));
    }

    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimantResponseBeforeSDO = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
            && caseData.getApplicant1ResponseDate() != null
            && caseData.getDrawDirectionsOrderRequired() == null
            && caseData.getReasonNotSuitableSDO() == null;

    public static final Predicate<CaseData> takenOfflineByStaffAfterSDO = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
            && caseData.getDrawDirectionsOrderRequired() != null
            && caseData.getReasonNotSuitableSDO() == null;

    public static final Predicate<CaseData> takenOfflineAfterNotSuitableForSdo = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
            && caseData.getDrawDirectionsOrderRequired() == null
            && caseData.getReasonNotSuitableSDO() != null
            && StringUtils.isNotBlank(caseData.getReasonNotSuitableSDO().getInput());
}
