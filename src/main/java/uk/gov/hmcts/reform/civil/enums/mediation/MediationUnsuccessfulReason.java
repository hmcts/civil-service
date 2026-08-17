package uk.gov.hmcts.reform.civil.enums.mediation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "MediationUnsuccessfulReasonsMultiSelect", generate = true)
@Getter
@RequiredArgsConstructor
public enum MediationUnsuccessfulReason {
    @CCD(label = "Party withdraws from mediation")
    PARTY_WITHDRAWS("Party withdraws from mediation"),
    @CCD(label = "Appointment no agreement reached")
    APPOINTMENT_NO_AGREEMENT("Appointment no agreement reached"),
    @CCD(label = "Appointment not assigned")
    APPOINTMENT_NOT_ASSIGNED("Appointment not assigned"),
    @CCD(label = "Mediation not attended - claimant 1 not contactable")
    NOT_CONTACTABLE_CLAIMANT_ONE("Mediation not attended - claimant 1 not contactable"),
    @CCD(label = "Mediation not attended - claimant 2 not contactable")
    NOT_CONTACTABLE_CLAIMANT_TWO("Mediation not attended - claimant 2 not contactable"),
    @CCD(label = "Mediation not attended - defendant 1 not contactable")
    NOT_CONTACTABLE_DEFENDANT_ONE("Mediation not attended - defendant 1 not contactable"),
    @CCD(label = "Mediation not attended - defendant 2 not contactable")
    NOT_CONTACTABLE_DEFENDANT_TWO("Mediation not attended - defendant 2 not contactable");

    private final String value;
}
