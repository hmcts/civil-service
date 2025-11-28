package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

@SuppressWarnings("java:S1214")
public non-sealed interface HearingPredicate extends CaseDataPredicate {

    @BusinessRule(
        group = "Hearing",
        summary = "Hearing listed and case in readiness",
        description = "Hearing has been listed and the case is in hearing readiness (no dismissals/taken offline)"
    )
    Predicate<CaseData> isInReadiness =
        CaseDataPredicate.Hearing.hasReference
            .and(CaseDataPredicate.Hearing.isListed)
            .and(CaseDataPredicate.Hearing.hasDismissedFeeDueDate.negate())
            .and(CaseDataPredicate.TakenOffline.dateExists.negate())
            .and(CaseDataPredicate.TakenOffline.byStaffDateExists.negate());

}
