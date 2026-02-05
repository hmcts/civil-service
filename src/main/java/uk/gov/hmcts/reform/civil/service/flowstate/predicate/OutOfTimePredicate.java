package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

@SuppressWarnings("java:S1214")
public non-sealed interface OutOfTimePredicate extends CaseDataPredicate {

    @BusinessRule(
        group = "OutOfTime",
        summary = "Applicant out of time and not being taken offline",
        description = "Applicant response deadline passed, applicant has not responded and staff offline date does not exist"
    )
    Predicate<CaseData> notBeingTakenOffline =
        CaseDataPredicate.Applicant.hasPassedResponseDeadline
            .and(CaseDataPredicate.Applicant.hasResponseDateApplicant1.negate())
            .and(CaseDataPredicate.TakenOffline.byStaffDateExists.negate());

}
