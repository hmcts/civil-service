package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.HearingBundle;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;
import java.util.Map;

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
    void shouldReadExistingCaseDataShape() {
        CaseData caseData = ObjectMapperFactory.instance().convertValue(Map.of(
            "disposalHearingBundleDJ", Map.of(
                "input", "existing text",
                "type", List.of("DOCUMENTS", "ELECTRONIC")
            )
        ), CaseData.class);

        assertThat(caseData.getDisposalHearingBundleDJ().getInput()).isEqualTo("existing text");
        assertThat(service.buildBundleInfo(caseData))
            .isEqualTo("an indexed bundle of documents, with each page clearly numbered"
                           + " / an electronic bundle of digital documents");
    }

    private CaseData caseDataWithBundle(List<String> types) {
        HearingBundle bundle = new HearingBundle();
        bundle.setType(types);
        return CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
            .disposalHearingBundleDJ(bundle)
            .build();
    }
}
