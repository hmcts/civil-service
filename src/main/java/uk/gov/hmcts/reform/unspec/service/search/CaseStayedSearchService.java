package uk.gov.hmcts.reform.unspec.service.search;

import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.service.CoreCaseDataService;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

@Service
public class CaseStayedSearchService extends ElasticSearchService {

    public CaseStayedSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    public SearchSourceBuilder query(int startIndex) {
        return new SearchSourceBuilder()
            .query(boolQuery()
                .must(rangeQuery("data.confirmationOfServiceDeadline").lt("now"))
                .must(matchQuery("state", "CREATED")))
            .fetchSource("reference", null)
            .from(startIndex);
    }
}
