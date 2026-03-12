package uk.gov.hmcts.reform.civil.notification.handlers.generatespecdjform;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.BOTH_DEFENDANTS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.NOTIFY_BOTH;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class GenerateSpecDJFormNotificationHelper {

    private static final String FIRST_DEFENDANT_LABEL = "First Defendant";
    private static final String SECOND_DEFENDANT_LABEL = "Second Defendant";

    public boolean hasSecondRespondent(CaseData caseData) {
        return caseData.getRespondent2() != null;
    }

    public boolean hasJudgmentForBothDefendants(CaseData caseData) {
        String selectedDefendant = getSelectedLabel(caseData);
        return StringUtils.startsWith(selectedDefendant, "Both")
            || StringUtils.equals(selectedDefendant, BOTH_DEFENDANTS)
            || StringUtils.startsWith(selectedDefendant, NOTIFY_BOTH);
    }

    public boolean hasSingleDefendantSelection(CaseData caseData) {
        return hasSecondRespondent(caseData) && !hasJudgmentForBothDefendants(caseData);
    }

    public boolean isFirstDefendantSelected(CaseData caseData) {
        return isSelectedDefendant(caseData, caseData.getRespondent1(), FIRST_DEFENDANT_LABEL, "first");
    }

    public boolean isSecondDefendantSelected(CaseData caseData) {
        return isSelectedDefendant(caseData, caseData.getRespondent2(), SECOND_DEFENDANT_LABEL, "second");
    }

    private boolean isSelectedDefendant(CaseData caseData, Party party, String fallbackLabel, String fallbackCode) {
        DynamicListElement selectedElement = getSelectedElement(caseData);
        if (selectedElement == null) {
            return false;
        }
        if (StringUtils.equalsIgnoreCase(selectedElement.getCode(), fallbackCode)) {
            return true;
        }
        String selectedLabel = selectedElement.getLabel();
        if (StringUtils.equals(selectedLabel, fallbackLabel)) {
            return true;
        }
        if (StringUtils.isBlank(selectedLabel) || party == null) {
            return false;
        }
        return StringUtils.equals(selectedLabel, getPartyNameBasedOnType(party));
    }

    private String getSelectedLabel(CaseData caseData) {
        DynamicListElement element = getSelectedElement(caseData);
        return element == null ? null : element.getLabel();
    }

    private DynamicListElement getSelectedElement(CaseData caseData) {
        DynamicList defendantDetails = caseData.getDefendantDetailsSpec();
        return defendantDetails == null ? null : defendantDetails.getValue();
    }
}
