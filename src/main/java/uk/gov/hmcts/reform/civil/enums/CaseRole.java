package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;

@Getter
public enum CaseRole {
    CREATOR,
    APPLICANTSOLICITORONE,
    RESPONDENTSOLICITORONE,
    RESPONDENTSOLICITORTWO,
    CLAIMANT,
    DEFENDANT;

    private String formattedName;

    public boolean isProfessionalRole() {
        return !(this == CLAIMANT || this == DEFENDANT);
    }

    CaseRole() {
        this.formattedName = String.format("[%s]", name());
    }
}
