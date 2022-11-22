package uk.gov.hmcts.reform.civil.service.flowstate;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

public class FlowLipPredicate {

    public static final Predicate<CaseData> isLipCase = caseData ->
        caseData.isApplicantNotRepresented();
}
