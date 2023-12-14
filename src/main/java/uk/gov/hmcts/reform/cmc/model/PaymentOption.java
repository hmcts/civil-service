package uk.gov.hmcts.reform.cmc.model;

public enum PaymentOption {
    IMMEDIATELY("Immediately"),
    BY_SPECIFIED_DATE("By a set date"),
    INSTALMENTS("By instalments");

    final String description;

    PaymentOption(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
