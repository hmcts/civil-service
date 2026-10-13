package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

class HearingFeeDueSearchServiceTest extends ElasticSearchServiceTest {

    @BeforeEach
    void setup() {
        searchService = new HearingFeeDueSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(boolQuery().must(matchQuery("state", "HEARING_READINESS")))
                        .must(existsQuery("data.hearingDate")));
        return new Query(query, List.of("reference"), fromValue);
    }

    @Test
    void shouldBuildQueryWithHearingDateExistsFilter() {
        Query query = searchService.query(0, "2026-06-29T00:00:00Z");
        String queryString = query.toString();

        assertThat(queryString)
            .contains("data.hearingDate")
            .doesNotContain("data.hearingDueDate");
    }
}
