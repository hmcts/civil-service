package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.SdoDJR2TrialCreditHire;

import java.util.List;
import java.util.Optional;

@Service
public class DjTrialTemplateFieldService {

    public boolean showCreditHireDetails(CaseData caseData) {
        return Optional.ofNullable(caseData.getSdoDJR2TrialCreditHire())
            .map(SdoDJR2TrialCreditHire::getDetailsShowToggle)
            .map(this::containsAddToggle)
            .orElse(false);
    }

    public boolean hasDateToToggle(CaseData caseData) {
        return caseData.getTrialHearingTimeDJ() != null
            && caseData.getTrialHearingTimeDJ().getDateToToggle() != null;
    }

    private boolean containsAddToggle(List<AddOrRemoveToggle> toggles) {
        return toggles != null && toggles.contains(AddOrRemoveToggle.ADD);
    }
}
