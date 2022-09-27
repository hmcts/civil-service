package uk.gov.hmcts.reform.civil.sampledata;

import uk.gov.hmcts.reform.civil.model.StatementOfTruth;

public class StatementOfTruthBuilder {

    public static StatementOfTruth.StatementOfTruthBuilder defaults() {
        return StatementOfTruth.builder()
            .name("Signer Name")
            .role("Signer Role");
    }

    public static StatementOfTruth.StatementOfTruthBuilder minimal() {
        return StatementOfTruth.builder()
            .name("a")
            .role("b");
    }

    public StatementOfTruth build() {
        return defaults().build();
    }
}
