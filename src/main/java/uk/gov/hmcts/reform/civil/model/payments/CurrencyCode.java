package uk.gov.hmcts.reform.civil.model.payments;

public enum CurrencyCode {
    GBP("GBP");

    private String code;

    CurrencyCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }
}
