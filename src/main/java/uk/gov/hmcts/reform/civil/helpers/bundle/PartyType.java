package uk.gov.hmcts.reform.civil.helpers.bundle;

public enum PartyType {
    CLAIMANT1("CL 1"),
    CLAIMANT2("CL 2"),
    DEFENDANT1("DF 1"),
    DEFENDANT2("DF 2");
    String displayName;

    public String getDisplayName() {
        return displayName;
    }

    PartyType(String displayName) {
        this.displayName = displayName;
    }
}
