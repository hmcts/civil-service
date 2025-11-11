package uk.gov.hmcts.reform.civil.service.sdo;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsWitnessStatement;

import java.util.List;

@Service
public class SdoSmallClaimsDirectionsService {

    public boolean hasSmallAdditionalDirections(CaseData caseData, SmallTrack direction) {
        List<SmallTrack> selections = caseData.getDrawDirectionsOrderSmallClaimsAdditionalDirections() != null
            ? caseData.getDrawDirectionsOrderSmallClaimsAdditionalDirections()
            : caseData.getSmallClaims();

        return selections != null
            && direction != null
            && selections.contains(direction);
    }

    public boolean hasSmallClaimsVariable(CaseData caseData, SmallClaimsVariable variable) {
        switch (variable) {
            case HEARING_TOGGLE:
                return caseData.getSmallClaimsHearingToggle() != null;
            case METHOD_TOGGLE:
                return true;
            case DOCUMENTS_TOGGLE:
                return caseData.getSmallClaimsDocumentsToggle() != null;
            case WITNESS_STATEMENT_TOGGLE:
                return caseData.getSmallClaimsWitnessStatementToggle() != null;
            case FLIGHT_DELAY_TOGGLE:
                return caseData.getSmallClaimsFlightDelayToggle() != null;
            case NUMBER_OF_WITNESSES_TOGGLE:
                SmallClaimsWitnessStatement witnessStatement = caseData.getSmallClaimsWitnessStatement();
                return witnessStatement != null && witnessStatement.getSmallClaimsNumberOfWitnessesToggle() != null;
            case ADD_NEW_DIRECTIONS:
                return caseData.getSmallClaimsAddNewDirections() != null;
            case MEDIATION_SECTION_TOGGLE:
                return caseData.getSmallClaimsMediationSectionStatement() != null;
            case WELSH_TOGGLE:
                return caseData.getSdoR2SmallClaimsUseOfWelshToggle() != null;
            default:
                throw new IllegalStateException("Unhandled small-claims variable: " + variable);
        }
    }
}
