package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;

@Getter
public enum CaseRole {
    CREATOR,
    APPLICANTSOLICITORONE,
    APPLICANTSOLICITORTWO,
    RESPONDENTSOLICITORONE,
    RESPONDENTSOLICITORTWO,
    APPLICANTSOLICITORONESPEC,
    APPLICANTSOLICITORTWOSPEC,
    RESPONDENTSOLICITORONESPEC,
    RESPONDENTSOLICITORTWOSPEC;

    private String formattedName;

    CaseRole() {
        this.formattedName = String.format("[%s]", name());
    }
}
