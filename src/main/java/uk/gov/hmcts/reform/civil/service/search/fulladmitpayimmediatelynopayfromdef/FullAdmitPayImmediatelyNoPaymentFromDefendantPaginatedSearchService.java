package uk.gov.hmcts.reform.civil.service.search.fulladmitpayimmediatelynopayfromdef;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchPaginatedStreamProvider;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

/**
 * Service that searches for full admit pay immediately cases where no payment has been received
 * from the defendant within the expected window.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FullAdmitPayImmediatelyNoPaymentFromDefendantPaginatedSearchService {

    private final ElasticSearchPaginatedStreamProvider elasticSearchPaginatedStreamProvider;
    private final FullAdmitPayImmediatelyNoPaymentFromDefendantQueryProvider queryProvider;

    @Value("${search.full-admit-pay-immediately-no-payment-from-def.pageSize:50}")
    private int pageSize;

    /**
     * Executes the search for cases with no payment from the defendant and returns a paginated result.
     *
     * @return an ElasticSearchResult containing the cases found
     */
    public ElasticSearchResult getElasticSearchResult() {
        return elasticSearchPaginatedStreamProvider.getPaginatedSearchResult(
            queryProvider,
            pageSize
        );
    }
}
