package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;

@Component
public class StandardDirectionOrderDJNotificationHelper {

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
}
