package uk.gov.hmcts.reform.civil.service.search.common;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseState;

import static org.assertj.core.api.Assertions.assertThat;

class CommonQueryConstructsTest {

    private final CommonQueryConstructs commonQueryConstructs = new CommonQueryConstructs();

    @Test
    void shouldReturnCorrectStateQuery() {
        BoolQueryBuilder query = commonQueryConstructs.beState(CaseState.JUDGMENT_REQUESTED);
        String json = query.toString();
        assertThat(json).contains("JUDGMENT_REQUESTED");
        assertThat(json).contains("\"state\"");
    }

    @Test
    void shouldReturnCorrectNoOngoingBusinessProcessQuery() {
        BoolQueryBuilder query = commonQueryConstructs.haveNoOngoingBusinessProcess();
        String json = query.toString();
        assertThat(json).contains("data.businessProcess");
        assertThat(json).contains("FINISHED");
        assertThat(json).contains("\"must_not\"");
    }
}
