package uk.gov.hmcts.reform.civil.service.flowstate;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

public class FlowLipPredicate {

    private FlowLipPredicate() {

    }

    public static final Predicate<CaseData> isLipCase = caseData ->
        caseData.isApplicantNotRepresented();
}
