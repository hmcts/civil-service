package uk.gov.hmcts.reform.civil.service.sdo;

/**
 * Enumerates the CaseData toggles used by the small-claims directions templates so we no longer pass
 * string identifiers through the service layer. Keeping these typed avoids copy/paste switch statements
 * and makes it clear which toggles are supported.
 */
public enum SmallClaimsVariable {
    HEARING_TOGGLE,
    METHOD_TOGGLE,
    DOCUMENTS_TOGGLE,
    WITNESS_STATEMENT_TOGGLE,
    FLIGHT_DELAY_TOGGLE,
    NUMBER_OF_WITNESSES_TOGGLE,
    ADD_NEW_DIRECTIONS,
    MEDIATION_SECTION_TOGGLE,
    WELSH_TOGGLE
}
