package uk.gov.hmcts.reform.civil.sampledata;

import uk.gov.hmcts.reform.civil.model.StatementOfTruth;

import static com.google.common.base.Strings.repeat;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataMaxEdgeCasesBuilder.MAX_ALLOWED;

public class StatementOfTruthBuilder {

    public static StatementOfTruth defaults() {
        return new StatementOfTruth()
            .setName("Signer Name")
            .setRole("Signer Role");
    }

    public static StatementOfTruth minimal() {
        return new StatementOfTruth()
            .setName("a")
            .setRole("b");
    }

    public static StatementOfTruth maximal() {
        return new StatementOfTruth()
            .setName(repeat("a", MAX_ALLOWED))
            .setRole(repeat("b", MAX_ALLOWED));
    }

    public StatementOfTruth build() {
        return defaults();
    }
}
