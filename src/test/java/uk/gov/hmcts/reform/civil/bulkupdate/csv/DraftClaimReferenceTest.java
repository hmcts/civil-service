package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DraftClaimReferenceTest {

    @Test
    void shouldMapNullExcelValuesToNull() {
        DraftClaimReference reference = new DraftClaimReference();

        reference.fromExcelRow(Map.of("caseReference", "draft-1"));

        assertThat(reference.getCaseReference()).isEqualTo("draft-1");
        assertThat(reference.getUserId()).isNull();
    }

    @Test
    void shouldExposeCaseReferenceAsDraftId() {
        UUID draftId = UUID.randomUUID();
        DraftClaimReference reference = new DraftClaimReference();
        reference.setDraftId(draftId.toString());

        assertThat(reference.getDraftUuid()).isEqualTo(draftId);
    }
}
