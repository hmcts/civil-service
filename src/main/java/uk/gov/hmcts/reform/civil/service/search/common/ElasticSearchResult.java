package uk.gov.hmcts.reform.civil.service.search.common;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.stream.Stream;

/**
 * Result of an ElasticSearch paginated search.
 * Contains the total number of results and a stream to consume them.
 * Note: the stream should be consumed only once.
 */
public record ElasticSearchResult(int totalResults, Stream<CaseDetails> caseDetailsStream) {

    public boolean isEmpty() {
        return totalResults == 0;
    }
}
