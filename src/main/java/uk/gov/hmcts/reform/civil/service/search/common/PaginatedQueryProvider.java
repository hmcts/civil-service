package uk.gov.hmcts.reform.civil.service.search.common;

import uk.gov.hmcts.reform.civil.model.search.PageToken;
import uk.gov.hmcts.reform.civil.model.search.PaginatedQuery;

public interface PaginatedQueryProvider {

    /**
     * Provides a paginated query for a given page token and page size.
     *
     * @param pageToken the token for the page to retrieve
     * @param pageSize the number of results to return in the page
     * @return the paginated query
     */
    PaginatedQuery getPaginatedQuery(PageToken pageToken, int pageSize);
}
