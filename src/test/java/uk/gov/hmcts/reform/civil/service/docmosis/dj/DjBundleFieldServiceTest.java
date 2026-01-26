package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingBundleType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingBundleDJ;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DjBundleFieldServiceTest {

    private final DjBundleFieldService service = new DjBundleFieldService();

    @Test
    void shouldReturnTextWhenAllTypesSelected() {
        CaseData caseData = caseDataWithBundle(List.of(
            DisposalHearingBundleType.DOCUMENTS,
            DisposalHearingBundleType.ELECTRONIC,
            DisposalHearingBundleType.SUMMARY
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

    private CaseData caseDataWithBundle(List<DisposalHearingBundleType> types) {
        DisposalHearingBundleDJ bundle = new DisposalHearingBundleDJ();
        bundle.setType(types);
        return CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
            .disposalHearingBundleDJ(bundle)
            .build();
    }
}
