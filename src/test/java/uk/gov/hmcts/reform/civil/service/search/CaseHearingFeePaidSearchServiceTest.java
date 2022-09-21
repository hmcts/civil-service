package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

class CaseHearingFeePaidSearchServiceTest extends ElasticSearchServiceTest {

    @BeforeEach
    void setup() {
        searchService = new CaseHearingFeePaidSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(rangeQuery("data.hearingDueDate").lt("now"))
                        .must(boolQuery().must(matchQuery("state", "HEARING_READINESS"))))
            .should(boolQuery()
                        .must(rangeQuery("data.hearingFee").lt("0"))
                        .must(boolQuery().must(matchQuery("state", "HEARING_READINESS"))));

        return new Query(query, List.of("reference"), fromValue);
    }
}
