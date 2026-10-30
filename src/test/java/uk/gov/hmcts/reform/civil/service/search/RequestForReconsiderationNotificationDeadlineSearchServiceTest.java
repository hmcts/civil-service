package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.search.common.CommonQueryConstructs;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

class RequestForReconsiderationNotificationDeadlineSearchServiceTest extends ElasticSearchServiceTest {

    @BeforeEach
    void setup() {
        searchService = new RequestForReconsiderationNotificationDeadlineSearchService(coreCaseDataService, new CommonQueryConstructs());
    }

    @Override
    protected Query buildQuery(int fromValue) {
        String deadlineCutoff = LocalDate.now(ZoneOffset.UTC).atTime(LocalTime.MIN).toString();
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(rangeQuery("data.requestForReconsiderationDeadline").lt(deadlineCutoff))
                        .mustNot(matchQuery("data.requestForReconsiderationDeadlineChecked", "Yes"))
                        .must(boolQuery().minimumShouldMatch(1)
                                  .should(matchQuery("state", "CASE_PROGRESSION"))
                                  .should(matchQuery("state", "HEARING_READINESS"))));
        return new Query(query, List.of("reference"), fromValue);
    }
}
