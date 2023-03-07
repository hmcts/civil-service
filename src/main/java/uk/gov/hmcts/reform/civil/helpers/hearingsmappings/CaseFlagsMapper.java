package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.hearingvalues.CaseFlags;

public class CaseFlagsMapper {

    private CaseFlagsMapper() {
        //NO-OP
    }

    public static CaseFlags getCaseFlags(CaseData caseData) {
        //todo
        return CaseFlags.builder()
            .build();
    }
}
