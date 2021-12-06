package uk.gov.hmcts.reform.civil.enums;

import uk.gov.hmcts.reform.civil.model.CaseData;
import java.util.ArrayList;
import java.util.List;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public enum UnrepresentedOrUnregisteredScenario {
    UNREPRESENTED,
    UNREGISTERED;

    public static List<String> getDefendantNames(UnrepresentedOrUnregisteredScenario scenario, CaseData caseData){
        List<String> defendantNames = new ArrayList<>();
        switch(scenario){
            case UNREPRESENTED:
                if(caseData.getRespondent1Represented() != YES)
                    defendantNames.add(caseData.getRespondent1().getPartyName());
                if(caseData.getRespondent2Represented() != YES && caseData.getRespondent2() != null)
                    defendantNames.add(caseData.getRespondent2().getPartyName());
                break;
            case UNREGISTERED:
                if(caseData.getRespondent1OrgRegistered() != YES
                    && caseData.getRespondent1OrganisationPolicy() == null
                    && caseData.getRespondent1Represented() == YES)
                    defendantNames.add(caseData.getRespondent1().getPartyName());
                if(caseData.getRespondent2OrgRegistered() != YES
                    && caseData.getRespondent2Represented() == YES
                    && caseData.getRespondent2OrganisationPolicy() == null
                    && caseData.getRespondent2() != null)
                    defendantNames.add(caseData.getRespondent2().getPartyName());
                break;
            default:
                break;
        }
        return defendantNames;
    }
}
