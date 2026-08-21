package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "CaseRoleType", generate = true)
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
