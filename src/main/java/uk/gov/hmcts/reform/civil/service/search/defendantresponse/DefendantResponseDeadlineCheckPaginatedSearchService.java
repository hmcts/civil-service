package uk.gov.hmcts.reform.civil.service.search.defendantresponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchPaginatedStreamProvider;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

/**
 * Service that searches for cases where the defendant response deadline has passed.
 * It identifies cases in the 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT' state that have not yet been checked.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DefendantResponseDeadlineCheckPaginatedSearchService {

    private final ElasticSearchPaginatedStreamProvider elasticSearchPaginatedStreamProvider;
    private final DefendantResponseDeadlineCheckQueryProvider defendantResponseDeadlineCheckQueryProvider;

    @Value("${search.defendant-response.pageSize:50}")
    private int pageSize;

    /**
     * Executes the search for expired defendant response deadlines and returns a paginated result.
     *
     * @return an ElasticSearchResult containing the cases found
     */
    public ElasticSearchResult getElasticSearchResult() {
        return elasticSearchPaginatedStreamProvider.getPaginatedSearchResult(
            defendantResponseDeadlineCheckQueryProvider,
            pageSize
        );
    }
}
