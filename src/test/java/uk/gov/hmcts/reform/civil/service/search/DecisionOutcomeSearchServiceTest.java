package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.Mockito.mockStatic;

class DecisionOutcomeSearchServiceTest extends ElasticSearchServiceTest {

    private static final ZonedDateTime FIXED_NOW = ZonedDateTime.parse("2026-01-19T12:04:11.611881Z");

    private MockedStatic<ZonedDateTime> zonedDateTimeMock;

    @BeforeEach
    void setup() {
        zonedDateTimeMock = mockStatic(ZonedDateTime.class);
        zonedDateTimeMock.when(() -> ZonedDateTime.now(ZoneOffset.UTC)).thenReturn(FIXED_NOW);
        searchService = new DecisionOutcomeSearchService(coreCaseDataService);
    }

    @AfterEach
    void tearDown() {
        zonedDateTimeMock.close();
    }

    @Override
    protected Query buildQuery(int fromValue) {
        String timeNow = FIXED_NOW.toString();
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                .must(rangeQuery("data.hearingDate").lte(timeNow))
                .must(boolQuery().must(matchQuery("state", "PREPARE_FOR_HEARING_CONDUCT_HEARING")))
                .must(boolQuery()
                    .should(rangeQuery("data.nextHearingDetails.hearingDateTime").lte(timeNow))
                    .should(boolQuery().mustNot(existsQuery("data.nextHearingDetails.hearingDateTime")))));

        return new Query(query, List.of("reference"), fromValue);
    }
}
