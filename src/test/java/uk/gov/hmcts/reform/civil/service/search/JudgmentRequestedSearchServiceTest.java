package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

class JudgmentRequestedSearchServiceTest extends ElasticSearchServiceTest {

    @BeforeEach
    void setup() {
        searchService = new JudgmentRequestedSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        LocalDate fortyEightHoursAgo = LocalDate.now().minusDays(2);
        BoolQueryBuilder query = boolQuery()
            .must(rangeQuery("data.activeJudgment.requestDate").lte(fortyEightHoursAgo));

        return new Query(query, List.of("reference"), fromValue);
    }
}
