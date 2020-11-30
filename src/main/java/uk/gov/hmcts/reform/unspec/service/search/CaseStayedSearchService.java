package uk.gov.hmcts.reform.unspec.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.model.search.Query;
import uk.gov.hmcts.reform.unspec.service.CoreCaseDataService;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

@Service
public class CaseStayedSearchService extends ElasticSearchService {

    public CaseStayedSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    public Query query(int startIndex) {
        return new Query(
            boolQuery()
                .must(rangeQuery("last_modified").lt("now-6M"))
                .must(beValidState()),
            List.of("reference"),
            startIndex
        );
    }

    public BoolQueryBuilder beValidState() {
        return boolQuery()
            .minimumShouldMatch(1)
            .should(matchQuery("state", "CREATED"))
            .should(matchQuery("state", "AWAITING_RESPONDENT_ACTION"));
    }
}
