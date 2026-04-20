package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

class CaseReadyBusinessProcessSearchServiceTest extends ElasticSearchServiceTest {

    private String capturedTimeNow;

    @BeforeEach
    void setup() {
        searchService = new CaseReadyBusinessProcessSearchService(coreCaseDataService) {
            @Override
            public Query query(int startIndex, String timeNow) {
                capturedTimeNow = timeNow;
                return super.query(startIndex, timeNow);
            }
        };
    }

    @Override
    protected Query buildQuery(int fromValue) {
        ZonedDateTime now = getCapturedTime();
        BoolQueryBuilder query = boolQuery()
            .must(matchQuery("data.businessProcess.status", "READY"))
            .must(rangeQuery("data.businessProcess.readyOn").lt(now.minusMinutes(5).toString()));
        return new Query(query, List.of(), fromValue);
    }

    private ZonedDateTime getCapturedTime() {
        return capturedTimeNow != null ? ZonedDateTime.parse(capturedTimeNow) : ZonedDateTime.now(ZoneOffset.UTC);
    }
}
