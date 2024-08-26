package uk.gov.hmcts.reform.civil.model.judgmentonline.cjes;

public enum RegistrationType {
    R("judgmentRegistered"),
    C("judgmentCancelled"),
    S("judgmentSatisfied"),
    M("judgmentModified"),
    A("adminOrderRegistered"),
    K("adminOrderRevoked"),
    V("adminOrderVaried");

    private String registrationType;

    RegistrationType(String registrationType) {
        this.registrationType = registrationType;
    }

    public String getRegistrationType() {
        return registrationType;
    }
}
