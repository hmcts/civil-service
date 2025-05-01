package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

class CoscApplicationSearchServiceTest extends ElasticSearchServiceTest {

    @BeforeEach
    void setup() {
        searchService = new CoscApplicationSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        BoolQueryBuilder query = boolQuery()
            .must(matchQuery("data.coSCApplicationStatus", "Active"))
            .must(rangeQuery("data.coscSchedulerDeadline").lt("now"));
        return new Query(query, List.of("reference"), fromValue);
    }

    @Override
    protected Query buildQueryInMediation(int fromValue, LocalDate date, boolean carmEnabled,
                                          boolean initialSearch,
                                          String searchAfterValue) {
        return null;
    }
}
