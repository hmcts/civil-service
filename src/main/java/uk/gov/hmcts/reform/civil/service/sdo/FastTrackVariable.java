package uk.gov.hmcts.reform.civil.service.sdo;

/**
 * Enumerates the fast-track toggles/sections referenced by Docmosis so callers can use a safe, typed API
 * instead of passing raw string identifiers.
 */
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

/**
 * Enumerates the fast-track toggles/sections referenced by Docmosis so callers can use a safe, typed API
 * instead of passing raw string identifiers.
 */
public enum FastTrackVariable {
    ALT_DISPUTE_RESOLUTION(caseData -> caseData.getFastTrackAltDisputeResolutionToggle() != null),
    VARIATION_OF_DIRECTIONS(caseData -> caseData.getFastTrackVariationOfDirectionsToggle() != null),
    SETTLEMENT(caseData -> caseData.getFastTrackSettlementToggle() != null),
    DISCLOSURE_OF_DOCUMENTS(caseData -> caseData.getFastTrackDisclosureOfDocumentsToggle() != null),
    WITNESS_OF_FACT(caseData -> caseData.getFastTrackWitnessOfFactToggle() != null),
    SCHEDULES_OF_LOSS(caseData -> caseData.getFastTrackSchedulesOfLossToggle() != null),
    COSTS(caseData -> caseData.getFastTrackCostsToggle() != null),
    TRIAL(caseData -> caseData.getFastTrackTrialToggle() != null),
    METHOD_TOGGLE(caseData -> true),
    ADD_NEW_DIRECTIONS(caseData -> caseData.getFastTrackAddNewDirections() != null),
    TRIAL_DATE_TO_TOGGLE(caseData -> caseData.getFastTrackHearingTime() != null
        && caseData.getFastTrackHearingTime().getDateToToggle() != null),
    WELSH_TOGGLE(caseData -> caseData.getSdoR2FastTrackUseOfWelshToggle() != null),
    TRIAL_BUNDLE_TOGGLE(caseData -> caseData.getFastTrackTrialBundleToggle() != null),
    PENAL_NOTICE_TOGGLE(caseData -> caseData.getFastTrackPenalNoticeToggle() != null);

    private final Predicate<CaseData> predicate;

    FastTrackVariable(Predicate<CaseData> predicate) {
        this.predicate = predicate;
    }

    public boolean isEnabled(CaseData caseData) {
        return predicate.test(caseData);
    }
}
