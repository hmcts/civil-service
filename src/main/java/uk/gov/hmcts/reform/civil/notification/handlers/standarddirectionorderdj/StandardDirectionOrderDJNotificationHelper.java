package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;

@Component
public class StandardDirectionOrderDJNotificationHelper {

    private static final String BOTH_DEFENDANTS = "Both Defendants";

    public boolean shouldNotifyApplicantSolicitor(CaseData caseData) {
        return !caseData.isApplicant1NotRepresented();
    }

    public boolean shouldNotifyRespondentSolicitorOne(CaseData caseData) {
        return !caseData.isRespondent1NotRepresented();
    }

    public boolean shouldNotifyRespondentSolicitorTwo(CaseData caseData) {
        return YesOrNo.YES.equals(caseData.getAddRespondent2())
            && isOneVTwoTwoLegalRep(caseData)
            && !caseData.isRespondent2NotRepresented();
    }

    /**
     * Returns true when the SDO DJ was requested against the given defendant
     * directly, or against "Both Defendants". Mirrors the gate that the legacy
     * StandardDirectionOrderDJDefendantNotificationHandler applied per-defendant.
     */
    public boolean isTargetDefendant(CaseData caseData, Party defendant) {
        String defendantName = Optional.ofNullable(defendant).map(Party::getPartyName).orElse(null);
        return isRequestedDefendant(caseData, defendantName) || isBothDefendantsSelected(caseData);
    }

    private boolean isRequestedDefendant(CaseData caseData, String defendantName) {
        return defendantName != null && getDefendantDetailsLabel(caseData)
            .map(defendantName::equals)
            .orElse(false);
    }

    private boolean isBothDefendantsSelected(CaseData caseData) {
        if (caseData.isRespondent1NotRepresented()) {
            return false;
        }
        return getDefendantDetailsLabel(caseData)
            .map(BOTH_DEFENDANTS::equals)
            .orElse(false);
    }

    private Optional<String> getDefendantDetailsLabel(CaseData caseData) {
        return Optional.ofNullable(caseData.getDefendantDetails())
            .map(DynamicList::getValue)
            .map(DynamicListElement::getLabel);
    }
}
