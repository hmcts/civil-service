package uk.gov.hmcts.reform.civil.service.search.common;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.model.search.PageToken;

public interface SearchAfterKeyProvider {

    /**
     * Extracts the search_after key from the last case of the current page.
     *
     * <p>
     * IMPORTANT: The field returned by this provider MUST match the field used
     * in the 'sort' clause of the query provided by {@link PaginatedQueryProvider}.
     * </p>
     *
     * @param caseDetails the last case of the current page
     * @return the page token containing the search_after key
     */
    PageToken getSearchAfterKey(CaseDetails caseDetails);
}
