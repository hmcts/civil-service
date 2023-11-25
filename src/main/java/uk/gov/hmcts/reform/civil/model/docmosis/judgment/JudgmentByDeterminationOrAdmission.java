package uk.gov.hmcts.reform.civil.model.docmosis.judgment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class JudgmentByDeterminationOrAdmission implements MappableObject {

    private final String caseNumber;
}
