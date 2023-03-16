package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.CaseNameUtils;

public class ServiceHearingsCaseLevelMapper {

    private ServiceHearingsCaseLevelMapper() {
        // no op
    }

    public static String getPublicCaseName(CaseData caseData) {
        return caseData.getCaseNamePublic() != null ? caseData.getCaseNamePublic()
            : CaseNameUtils.buildCaseNamePublic(caseData);
    }
}
