package uk.gov.hmcts.reform.civil.sampledata;

import uk.gov.hmcts.reform.civil.model.StatementOfTruth;

import static com.google.common.base.Strings.repeat;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataEdgeCasesBuilder.MAX_ALLOWED;

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

    public static StatementOfTruth.StatementOfTruthBuilder maximal() {
        return StatementOfTruth.builder()
            .name(repeat("a", MAX_ALLOWED))
            .role(repeat("b", MAX_ALLOWED));
    }

    public StatementOfTruth build() {
        return defaults().build();
    }
}
