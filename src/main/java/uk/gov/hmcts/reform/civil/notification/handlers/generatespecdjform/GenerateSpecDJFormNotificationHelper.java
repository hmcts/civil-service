package uk.gov.hmcts.reform.civil.notification.handlers.generatespecdjform;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import java.util.Optional;

@Component
public class GenerateSpecDJFormNotificationHelper {

    public boolean hasSecondRespondent(CaseData caseData) {
        return caseData.getRespondent2() != null;
    }

    public boolean hasJudgmentForBothDefendants(CaseData caseData) {
        return getSelectedDefendant(caseData)
            .map(label -> label.startsWith("Both"))
            .orElse(false);
    }

    public boolean hasSingleDefendantSelection(CaseData caseData) {
        return hasSecondRespondent(caseData) && !hasJudgmentForBothDefendants(caseData);
    }

    private Optional<String> getSelectedDefendant(CaseData caseData) {
        return Optional.ofNullable(caseData.getDefendantDetailsSpec())
            .map(DynamicList::getValue)
            .map(DynamicListElement::getLabel);
    }
}
