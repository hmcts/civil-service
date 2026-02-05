package uk.gov.hmcts.reform.civil.service.dj;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.BOTH_DEFENDANTS;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;

@Service
public class DjNotificationRecipientService {

    public String getClaimantEmail(CaseData caseData) {
        return caseData.isApplicant1NotRepresented()
            ? caseData.getClaimantUserDetails().getEmail()
            : caseData.getApplicantSolicitor1UserDetails().getEmail();
    }

    public String getRespondent1Email(CaseData caseData) {
        return caseData.isRespondent1NotRepresented()
            ? caseData.getDefendantUserDetails().getEmail()
            : caseData.getRespondentSolicitor1EmailAddress();
    }

    public String getRespondent2Email(CaseData caseData) {
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        if (scenario == MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP) {
            return caseData.getRespondent1EmailAddress();
        }
        return YesOrNo.YES.equals(caseData.getRespondent2Represented())
            ? caseData.getRespondentSolicitor2EmailAddress()
            : caseData.getRespondent2().getPartyEmail();
    }

    public boolean shouldNotifyRespondent1(CaseData caseData) {
        return checkDefendantRequested(caseData, caseData.getRespondent1().getPartyName())
            || checkIfBothDefendants(caseData);
    }

    public boolean shouldNotifyRespondent2(CaseData caseData) {
        if (!YesOrNo.YES.equals(caseData.getAddRespondent2())) {
            return false;
        }
        return checkDefendantRequested(caseData, caseData.getRespondent2().getPartyName())
            || checkIfBothDefendants(caseData);
    }

    private boolean checkIfBothDefendants(CaseData caseData) {
        if (caseData.isRespondent1NotRepresented()) {
            return false;
        }
        DynamicList defendantDetails = caseData.getDefendantDetails();
        DynamicListElement selection = defendantDetails != null ? defendantDetails.getValue() : null;
        return selection != null && BOTH_DEFENDANTS.equals(selection.getLabel());
    }

    private boolean checkDefendantRequested(CaseData caseData, String defendantName) {
        DynamicList defendantDetails = caseData.getDefendantDetails();
        if (defendantDetails == null || defendantDetails.getValue() == null) {
            return false;
        }
        return defendantName.equals(defendantDetails.getValue().getLabel());
    }
}
