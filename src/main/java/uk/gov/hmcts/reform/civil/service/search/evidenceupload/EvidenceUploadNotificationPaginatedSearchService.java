package uk.gov.hmcts.reform.civil.service.search.evidenceupload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchPaginatedStreamProvider;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

/**
 * Service that searches for cases needing an evidence upload notification.
 * It identifies cases which have had a document uploaded in the last 7 days and have not yet been notified.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EvidenceUploadNotificationPaginatedSearchService {

    private final ElasticSearchPaginatedStreamProvider elasticSearchPaginatedStreamProvider;
    private final EvidenceUploadNotificationQueryProvider evidenceUploadNotificationQueryProvider;

    @Value("${search.evidence-upload.pageSize:50}")
    private int pageSize;

    /**
     * Executes the search for cases awaiting an evidence upload notification and returns a paginated result.
     *
     * @return an ElasticSearchResult containing the cases found
     */
    public ElasticSearchResult getElasticSearchResult() {
        return elasticSearchPaginatedStreamProvider.getPaginatedSearchResult(
            evidenceUploadNotificationQueryProvider,
            pageSize
        );
    }
}
