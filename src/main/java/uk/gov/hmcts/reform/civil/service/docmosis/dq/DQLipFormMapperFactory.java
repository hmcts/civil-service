package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import uk.gov.hmcts.reform.civil.model.CaseData;

public class DQLipFormMapperFactory {

    public static String CLAIMANT_LIP_RESPONSE_PROCESS = "CLAIMANT_RESPONSE_CUI";

    public DQLipFormMapper getDQLipFormMapper(CaseData caseData) {
        if (isClaimantLipResponse(caseData)) {
            return new DQLipClaimantFormMapper();
        }
        return new DQLipDefendantFormMapper();
    }

    private boolean isClaimantLipResponse(CaseData caseData) {
        return CLAIMANT_LIP_RESPONSE_PROCESS.equals(caseData.getCurrentCamundaBusinessProcessName());
    }
}
