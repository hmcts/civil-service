package uk.gov.hmcts.reform.civil.service.search.hearingfee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchPaginatedStreamProvider;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

/**
 * Service that searches for cases where the hearing fee needs to be checked.
 * It identifies cases in the 'HEARING_READINESS' state.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HearingFeeDuePaginatedSearchService {

    private final ElasticSearchPaginatedStreamProvider elasticSearchPaginatedStreamProvider;
    private final HearingFeeDueQueryProvider hearingFeeDueQueryProvider;

    @Value("${search.hearing-fee.pageSize:50}")
    private int pageSize;

    /**
     * Executes the search for cases awaiting a hearing fee check and returns a paginated result.
     *
     * @return an ElasticSearchResult containing the cases found
     */
    public ElasticSearchResult getElasticSearchResult() {
        return elasticSearchPaginatedStreamProvider.getPaginatedSearchResult(
            hearingFeeDueQueryProvider,
            pageSize
        );
    }
}
