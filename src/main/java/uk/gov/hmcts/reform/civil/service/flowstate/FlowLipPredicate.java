package uk.gov.hmcts.reform.civil.service.flowstate;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlowLipPredicate {

    public static final Predicate<CaseData> isLipCase = caseData ->
        caseData.isApplicantNotRepresented();
}
