package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;

class SdoNarrativeServiceTest {

    private final SdoNarrativeService service = new SdoNarrativeService();

    @Test
    void shouldReturnPlaceholderConfirmationHeader() {
        String result = service.buildConfirmationHeader(CaseData.builder().build());

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnPlaceholderConfirmationBody() {
        String result = service.buildConfirmationBody(CaseData.builder().build());

        assertThat(result).isEmpty();
    }
}

