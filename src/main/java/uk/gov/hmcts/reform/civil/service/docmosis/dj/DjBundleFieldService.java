package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.utils.BundleTypeTextUtils.buildBundleTypeText;

/**
 * Responsible for bundle-related derived fields shared between disposal and trial templates.
 */
@Service
public class DjBundleFieldService {

    public String buildBundleInfo(CaseData caseData) {
        if (caseData.getTrialHearingTrialDJ() != null) {
            return buildBundleTypeText(caseData.getTrialHearingTrialDJ().getType());
        }
        return "";
    }
}
