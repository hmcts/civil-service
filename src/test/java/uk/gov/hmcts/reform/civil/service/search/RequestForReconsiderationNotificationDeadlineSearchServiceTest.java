package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

class RequestForReconsiderationNotificationDeadlineSearchServiceTest extends ElasticSearchServiceTest {

    @BeforeEach
    void setup() {
        searchService = new RequestForReconsiderationNotificationDeadlineSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(rangeQuery("data.requestForReconsiderationDeadline").lt(LocalDate.now()
                                                                       .atTime(LocalTime.MIN)
                                                                       .toString()))
                        .mustNot(matchQuery("data.requestForReconsiderationDeadlineChecked", "Yes"))
                        .must(boolQuery().must(matchQuery("state", "CASE_PROGRESSION"))));
        return new Query(query, List.of("reference"), fromValue);
    }

    @Override
    protected Query buildQueryInMediation(int fromValue, LocalDate date, boolean carmEnabled,
                                          boolean initialSearch,
                                          String searchAfterValue) {
        return null;
    }
}
