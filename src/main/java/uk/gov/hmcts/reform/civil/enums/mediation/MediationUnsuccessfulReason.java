package uk.gov.hmcts.reform.civil.enums.mediation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediationUnsuccessfulReason {
    PARTY_WITHDRAWS("Party withdraws from mediation"),
    APPOINTMENT_NO_AGREEMENT("Appointment no agreement reached"),
    APPOINTMENT_NOT_ASSIGNED("Appointment not assigned"),
    NOT_CONTACTABLE_CLAIMANT_ONE("Mediation not attended - claimant 1 not contactable"),
    NOT_CONTACTABLE_CLAIMANT_TWO("Mediation not attended - claimant 2 not contactable"),
    NOT_CONTACTABLE_DEFENDANT_ONE("Mediation not attended - defendant 1 not contactable"),
    NOT_CONTACTABLE_DEFENDANT_TWO("Mediation not attended - defendant 2 not contactable");

    private final String value;
}
