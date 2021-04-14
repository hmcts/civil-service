package uk.gov.hmcts.reform.unspec.sampledata;

import uk.gov.hmcts.reform.unspec.model.StatementOfTruth;

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
