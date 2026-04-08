package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.dj.CaseManagementOrderAdditional;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalAndTrialHearingDJToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.dj.CaseManagementOrderAdditional.OrderTypeTrialAdditionalDirectionsEmployersLiability;
import static uk.gov.hmcts.reform.civil.enums.dj.DisposalAndTrialHearingDJToggle.SHOW;

/**
 * Provides toggle-based helpers shared across DJ templates.
 */
@Service
public class DjDirectionsToggleService {

    public boolean isToggleEnabled(List<DisposalAndTrialHearingDJToggle> toggles) {
        return nonNull(toggles) && !toggles.isEmpty() && toggles.get(0) == SHOW;
    }

    public boolean hasAdditionalDirections(CaseData caseData) {
        return caseData.getDisposalHearingAddNewDirectionsDJ() != null
            || caseData.getTrialHearingAddNewDirectionsDJ() != null;
    }

    public boolean hasEmployerLiability(List<CaseManagementOrderAdditional> additionalDirections) {
        return nonNull(additionalDirections)
            && additionalDirections.contains(OrderTypeTrialAdditionalDirectionsEmployersLiability);
    }
}
