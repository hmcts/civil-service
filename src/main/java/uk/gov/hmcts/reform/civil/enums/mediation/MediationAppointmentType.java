package uk.gov.hmcts.reform.civil.enums.mediation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediationAppointmentType {
    AUTO("Auto Mediation"),
    BULK("Bulk Mediation");

    private final String displayedValue;
}
