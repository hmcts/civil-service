package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingTrial;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DjBundleFieldServiceTest {

    private final DjBundleFieldService service = new DjBundleFieldService();

    @Test
    void shouldReturnTextWhenAllTypesSelected() {
        CaseData caseData = caseDataWithBundle(List.of(
            "DOCUMENTS",
            "ELECTRONIC",
            "SUMMARY"
        ));

        assertThat(service.buildBundleInfo(caseData))
            .contains("indexed bundle")
            .contains("electronic bundle")
            .contains("case summary");
    }

    @Test
    void shouldReturnEmptyWhenBundleMissing() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();

        assertThat(service.buildBundleInfo(caseData)).isEmpty();
    }

    @Test
    private CaseData caseDataWithBundle(List<String> types) {
        TrialHearingTrial bundle = new TrialHearingTrial();
        bundle.setType(types);
        return CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
            .trialHearingTrialDJ(bundle)
            .build();
    }
}
