package uk.gov.hmcts.reform.civil.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DefendantRepresentationType {

    COUNSEL_FOR_DEFENDANT("Counsel for defendant"),
    SOLICITOR_FOR_DEFENDANT("Solicitor for defendant"),
    COST_DRAFTSMAN_FOR_THE_DEFENDANT("Cost draftsman for the defendant"),
    THE_DEFENDANT_IN_PERSON("The defendant in person"),
    LAY_REPRESENTATIVE_FOR_THE_DEFENDANT("Lay representative for the defendant"),
    DEFENDANT_NOT_ATTENDING("Defendant not attending");

    private final String displayedValue;
}
