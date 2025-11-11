package uk.gov.hmcts.reform.civil.service.sdo;

/**
 * Enumerates the fast-track toggles/sections referenced by Docmosis so callers can use a safe, typed API
 * instead of passing raw string identifiers.
 */
public enum FastTrackVariable {
    ALT_DISPUTE_RESOLUTION,
    VARIATION_OF_DIRECTIONS,
    SETTLEMENT,
    DISCLOSURE_OF_DOCUMENTS,
    WITNESS_OF_FACT,
    SCHEDULES_OF_LOSS,
    COSTS,
    TRIAL,
    METHOD_TOGGLE,
    ADD_NEW_DIRECTIONS,
    TRIAL_DATE_TO_TOGGLE,
    WELSH_TOGGLE,
    TRIAL_BUNDLE_TOGGLE
}
