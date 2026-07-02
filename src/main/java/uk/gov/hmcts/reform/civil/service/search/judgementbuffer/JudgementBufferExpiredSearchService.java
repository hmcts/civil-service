package uk.gov.hmcts.reform.civil.service.search.judgementbuffer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchPaginatedStreamProvider;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

/**
 * Service that searches for cases where the judgement buffer has expired.
 * It identifies cases in the 'JUDGMENT_REQUESTED' state that have been in that state for more than 48 hours.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JudgementBufferExpiredSearchService {

    private final ElasticSearchPaginatedStreamProvider elasticSearchPaginatedStreamProvider;
    private final JudgementBufferExpiredQueryProvider judgementBufferExpiredQueryProvider;

    @Value("${search.judgementBuffer.pageSize:50}")
    private int pageSize;

    /**
     * Executes the search for expired judgements and returns a paginated result.
     *
     * @return an ElasticSearchResult containing the cases found
     */
    public ElasticSearchResult getElasticSearchResult() {
        return elasticSearchPaginatedStreamProvider.getPaginatedSearchResult(
            judgementBufferExpiredQueryProvider,
            pageSize
        );
    }
}
