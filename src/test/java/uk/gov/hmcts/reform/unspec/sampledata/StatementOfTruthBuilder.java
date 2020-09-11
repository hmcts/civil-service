package uk.gov.hmcts.reform.unspec.sampledata;

import uk.gov.hmcts.reform.unspec.model.StatementOfTruth;

public class StatementOfTruthBuilder {

    public static StatementOfTruth.StatementOfTruthBuilder builder() {
        return StatementOfTruth.builder()
            .name("Signer Name")
            .role("Signer Role");
    }

    public StatementOfTruth build() {
        return builder().build();
    }
}
