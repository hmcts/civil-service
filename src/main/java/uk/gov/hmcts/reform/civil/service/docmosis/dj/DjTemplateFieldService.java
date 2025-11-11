package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.dj.CaseManagementOrderAdditional;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalAndTrialHearingDJToggle;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingBundleType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingBundleDJ;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.dj.CaseManagementOrderAdditional.OrderTypeTrialAdditionalDirectionsEmployersLiability;
import static uk.gov.hmcts.reform.civil.enums.dj.DisposalAndTrialHearingDJToggle.SHOW;

@Service
public class DjTemplateFieldService {

    public boolean isJudge(UserDetails userDetails) {
        if (userDetails == null || userDetails.getRoles() == null) {
            return false;
        }
        return userDetails.getRoles().stream()
            .anyMatch(role -> role != null && role.toLowerCase().contains("judge"));
    }

    public String buildBundleInfo(CaseData caseData) {
        DisposalHearingBundleDJ bundle = caseData.getDisposalHearingBundleDJ();
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

    public boolean isToggleEnabled(List<DisposalAndTrialHearingDJToggle> toggle) {
        return nonNull(toggle) && !toggle.isEmpty() && toggle.get(0) == SHOW;
    }

    public boolean hasAdditionalDirections(CaseData caseData) {
        return caseData.getDisposalHearingAddNewDirectionsDJ() != null
            || caseData.getTrialHearingAddNewDirectionsDJ() != null;
    }

    public boolean hasEmployerLiability(List<CaseManagementOrderAdditional> list) {
        return nonNull(list) && list.contains(OrderTypeTrialAdditionalDirectionsEmployersLiability);
    }

}
