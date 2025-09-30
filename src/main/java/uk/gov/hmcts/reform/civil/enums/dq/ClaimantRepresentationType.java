package uk.gov.hmcts.reform.civil.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClaimantRepresentationType {

    COUNSEL_FOR_CLAIMANT("Counsel for claimant"),
    SOLICITOR_FOR_CLAIMANT("Solicitor for claimant"),
    COST_DRAFTSMAN_FOR_THE_CLAIMANT("Cost draftsman for the claimant"),
    THE_CLAIMANT_IN_PERSON("The claimant in person"),
    LAY_REPRESENTATIVE_FOR_THE_CLAIMANT("Lay representative for the claimant"),
    CLAIMANT_NOT_ATTENDING("Claimant not attending");

    private final String displayedValue;
}
