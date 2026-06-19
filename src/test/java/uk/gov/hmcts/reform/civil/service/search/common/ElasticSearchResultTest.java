package uk.gov.hmcts.reform.civil.service.search.common;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ElasticSearchResultTest {

    @Test
    void shouldReturnStream_whenAccessedFirstTime() {
        ElasticSearchResult result = new ElasticSearchResult(Stream.empty(), 0);

        assertThat(result.caseDetailsStream()).isNotNull();
    }

    @Test
    void shouldThrowException_whenAccessedSecondTime() {
        ElasticSearchResult result = new ElasticSearchResult(Stream.empty(), 0);

        result.caseDetailsStream();

        assertThrows(IllegalStateException.class, result::caseDetailsStream);
    }

    @Test
    void shouldReturnCorrectIsEmpty() {
        ElasticSearchResult resultWithCases = new ElasticSearchResult(Stream.of(CaseDetails.builder().build()), 1);
        ElasticSearchResult resultWithoutCases = new ElasticSearchResult(Stream.empty(), 0);

        assertThat(resultWithCases.isEmpty()).isFalse();
        assertThat(resultWithoutCases.isEmpty()).isTrue();
    }
}
