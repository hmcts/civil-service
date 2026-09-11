package uk.gov.hmcts.reform.civil.service.search.takecaseoffline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchPaginatedStreamProvider;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

@Service
@Slf4j
@RequiredArgsConstructor
public class TakeCaseOfflineSchedulerSearchService {

    private final ElasticSearchPaginatedStreamProvider elasticSearchPaginatedStreamProvider;
    private final TakeCaseOfflineQueryProvider takeCaseOfflineQueryProvider;

    @Value("${search.take-case-offline.pageSize:50}")
    private int pageSize;

    public ElasticSearchResult getElasticSearchResult() {
        return elasticSearchPaginatedStreamProvider.getPaginatedSearchResult(
            takeCaseOfflineQueryProvider,
            pageSize
        );
    }
}
