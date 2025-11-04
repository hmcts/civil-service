package uk.gov.hmcts.reform.civil.service.dj;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Service
public class DjDirectionOrderService {

    public CaseData prepareDisposalDraft(CaseData caseData) {
        return caseData;
    }

    public CaseData prepareTrialDraft(CaseData caseData) {
        return caseData;
    }
}
