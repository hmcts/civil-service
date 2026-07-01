package uk.gov.hmcts.reform.civil.service.search.common;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * Result of an ElasticSearch paginated search.
 * Contains the total number of results and a stream to consume them.
 * Note: the stream should be consumed only once.
 */
public final class ElasticSearchResult implements TaskResult<CaseDetails> {

    private final Stream<CaseDetails> caseDetailsStream;
    private final int totalResults;
    private final AtomicBoolean consumed = new AtomicBoolean(false);

    public ElasticSearchResult(Stream<CaseDetails> caseDetailsStream, int totalResults) {
        this.totalResults = totalResults;
        this.caseDetailsStream = caseDetailsStream;
    }

    public int totalResults() {
        return totalResults;
    }

    public Stream<CaseDetails> caseDetailsStream() {
        if (consumed.getAndSet(true)) {
            throw new IllegalStateException("Stream has already been consumed");
        }
        return caseDetailsStream;
    }

    @Override
    public Stream<CaseDetails> itemStream() {
        return caseDetailsStream();
    }

    public boolean isEmpty() {
        return totalResults == 0;
    }
}
