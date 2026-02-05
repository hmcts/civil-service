package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingBundleType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingBundleDJ;

import java.util.List;
import java.util.Optional;

/**
 * Responsible for bundle-related derived fields shared between disposal and trial templates.
 */
@Service
public class DjBundleFieldService {

    public String buildBundleInfo(CaseData caseData) {
        DisposalHearingBundleDJ bundle = Optional.ofNullable(caseData.getDisposalHearingBundleDJ()).orElse(null);
        if (bundle == null || bundle.getType() == null || bundle.getType().isEmpty()) {
            return "";
        }

        List<DisposalHearingBundleType> types = bundle.getType();
        if (types.size() == 3) {
            return uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingBundleType.DOCUMENTS.getLabel()
                + " / " + uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingBundleType.ELECTRONIC.getLabel()
                + " / " + DisposalHearingBundleType.SUMMARY.getLabel();
        }
        if (types.size() == 2) {
            return types.get(0).getLabel() + " / " + types.get(1).getLabel();
        }
        return types.get(0).getLabel();
    }
}
