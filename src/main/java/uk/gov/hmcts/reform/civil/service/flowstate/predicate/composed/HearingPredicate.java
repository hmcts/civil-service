package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

public final class HearingPredicate {

    @BusinessRule(
        group = "Hearing",
        summary = "Case in hearing readiness",
        description = "Hearing reference present, hearing listed and case not dismissed or taken offline"
    )
    public static final Predicate<CaseData> isInReadiness =
        CaseDataPredicate.Hearing.hasReference
            .and(CaseDataPredicate.Hearing.isListed)
            .and(CaseDataPredicate.Hearing.hasDismissedFeeDueDate.negate())
            .and(CaseDataPredicate.TakenOffline.dateExists.negate())
            .and(CaseDataPredicate.TakenOffline.byStaffDateExists.negate());

    private HearingPredicate() {
    }
}
