package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.search.common.CommonQueryConstructs;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.junit.jupiter.api.Assertions.assertFalse;

class HearingFeeDueSearchServiceTest extends ElasticSearchServiceTest {

    private final CommonQueryConstructs commonQueryConstructs = new CommonQueryConstructs();

    @BeforeEach
    void setup() {
        searchService = new HearingFeeDueSearchService(coreCaseDataService, commonQueryConstructs);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(boolQuery().must(matchQuery("state", "HEARING_READINESS")))
                        .must(commonQueryConstructs.haveNoOngoingBusinessProcess()));
        return new Query(query, List.of("reference"), fromValue);
    }

    @Test
    void testQuery() {
        Query expectedQuery = buildQuery(0);
        String queryString = expectedQuery.toString();
        assertFalse(queryString.contains("data.hearingDueDate"));
    }
}
