package uk.gov.hmcts.reform.civil.service.sdo;

/**
 * Enumerates the CaseData toggles used by the small-claims directions templates so we no longer pass
 * string identifiers through the service layer. Keeping these typed avoids copy/paste switch statements
 * and makes it clear which toggles are supported.
 */
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsWitnessStatement;

import java.util.function.Predicate;

/**
 * Enumerates the CaseData toggles used by the small-claims directions templates so we no longer pass
 * string identifiers through the service layer. Keeping these typed avoids copy/paste switch statements
 * and makes it clear which toggles are supported.
 */
public enum SmallClaimsVariable {
    HEARING_TOGGLE(caseData -> caseData.getSmallClaimsHearingToggle() != null),
    METHOD_TOGGLE(caseData -> true),
    DOCUMENTS_TOGGLE(caseData -> caseData.getSmallClaimsDocumentsToggle() != null),
    WITNESS_STATEMENT_TOGGLE(caseData -> caseData.getSmallClaimsWitnessStatementToggle() != null),
    FLIGHT_DELAY_TOGGLE(caseData -> caseData.getSmallClaimsFlightDelayToggle() != null),
    NUMBER_OF_WITNESSES_TOGGLE(caseData -> {
        SmallClaimsWitnessStatement witnessStatement = caseData.getSmallClaimsWitnessStatement();
        return witnessStatement != null && witnessStatement.getSmallClaimsNumberOfWitnessesToggle() != null;
    }),
    ADD_NEW_DIRECTIONS(caseData -> caseData.getSmallClaimsAddNewDirections() != null),
    MEDIATION_SECTION_TOGGLE(caseData -> caseData.getSmallClaimsMediationSectionStatement() != null),
    WELSH_TOGGLE(caseData -> caseData.getSdoR2SmallClaimsUseOfWelshToggle() != null);

    private final Predicate<CaseData> predicate;

    SmallClaimsVariable(Predicate<CaseData> predicate) {
        this.predicate = predicate;
    }

    public boolean isEnabled(CaseData caseData) {
        return predicate.test(caseData);
    }
}
