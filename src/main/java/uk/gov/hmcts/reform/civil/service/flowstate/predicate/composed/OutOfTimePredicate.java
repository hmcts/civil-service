package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

public final class OutOfTimePredicate {

    @BusinessRule(
        group = "OutOfTime",
        summary = "Applicant out of time and not being taken offline",
        description = "Applicant response deadline passed, applicant has not responded and staff offline date does not exist"
    )
    public static final Predicate<CaseData> notBeingTakenOffline =
        CaseDataPredicate.Applicant.hasPassedResponseDeadline
            .and(CaseDataPredicate.Applicant.hasResponseDate.negate())
            .and(CaseDataPredicate.TakenOffline.byStaffDateExists.negate());

    @BusinessRule(
        group = "OutOfTime",
        summary = "Applicant out of time processed by Camunda",
        description = "Case has been taken offline (takenOffline date present) indicating automated processing of out-of-time cases"
    )
    public static final Predicate<CaseData> processedByCamunda = CaseDataPredicate.TakenOffline.dateExists;

    private OutOfTimePredicate() {
    }
}
