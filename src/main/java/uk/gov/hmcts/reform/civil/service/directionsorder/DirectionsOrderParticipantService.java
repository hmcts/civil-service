package uk.gov.hmcts.reform.civil.service.directionsorder;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;

@Service
public class DirectionsOrderParticipantService {

    public String buildApplicantVRespondentText(CaseData caseData) {
        MultiPartyScenario scenario = MultiPartyScenario.getMultiPartyScenario(caseData);
        return switch (scenario) {
            case ONE_V_ONE -> formatOneVOne(caseData);
            case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP -> formatOneVTwo(caseData);
            case TWO_V_ONE -> formatTwoVOne(caseData);
            default -> throw new CallbackException("Invalid participants");
        };
    }

    private String formatOneVOne(CaseData caseData) {
        return caseData.getApplicant1().getPartyName()
            + " v "
            + caseData.getRespondent1().getPartyName();
    }

    private String formatOneVTwo(CaseData caseData) {
        return caseData.getApplicant1().getPartyName()
            + " v "
            + caseData.getRespondent1().getPartyName()
            + " and "
            + caseData.getRespondent2().getPartyName();
    }

    private String formatTwoVOne(CaseData caseData) {
        return caseData.getApplicant1().getPartyName()
            + " and "
            + caseData.getApplicant2().getPartyName()
            + " v "
            + caseData.getRespondent1().getPartyName();
    }
}
