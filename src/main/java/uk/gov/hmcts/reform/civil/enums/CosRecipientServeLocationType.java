package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CosRecipientServeLocationType {
    USUAL_RESIDENCE("Usual Residence"),
    LAST_KNOWN_RESIDENCE("Last known residence"),
    PLACE_OF_BUSINESS_WITH_CONNECTION("Place of business of the partnership/company/corporation "
                                          + "within the jurisdiction with a connection to claim"),
    PRINCIPAL_OFFICE_COMPANY("Principal office of the company"),
    PRINCIPAL_OFFICE_CORPORATION("Principal office of the corporation"),
    PRINCIPAL_OFFICE_PARTNERSHIP("Principal office of the partnership"),
    LAST_KNOWN_PRINCIPAL_PLACE_BUSINESS("Last known principal place of business"),
    LAST_KNOWN_PLACE_BUSINESS("Last known place of business"),
    PRINCIPAL_PLACE_OF_BUSINESS("Principal place of business"),
    PLACE_OF_BUSINESS("Place of business"),
    EMAIL("Email"),
    OTHER("Other");

    private final String label;
}
