package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.search.calculator.SearchDateTimeCalculator;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static uk.gov.hmcts.reform.civil.helpers.LocalDateTimeHelper.LOCAL_ZONE;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

class JudgementBufferExpiredSearchServiceTest extends ElasticSearchServiceTest {

    private static final ZonedDateTime FIXED_NOW = ZonedDateTime.of(2025, 7, 8, 10, 0, 0, 0, ZoneOffset.UTC);
    private static final ZonedDateTime FIXED_NOW_LONDON = FIXED_NOW.withZoneSameInstant(LOCAL_ZONE);
    private MockedStatic<ZonedDateTime> zonedDateTimeMock;

    @BeforeEach
    void setup() {
        SearchDateTimeCalculator dateTimeCalculator = Mockito.mock(SearchDateTimeCalculator.class);
        Mockito.when(dateTimeCalculator.minusWorkingHours(FIXED_NOW_LONDON, 48))
            .thenReturn(FIXED_NOW_LONDON.minusDays(2));
        searchService = new JudgementBufferExpiredSearchService(coreCaseDataService, dateTimeCalculator);
        zonedDateTimeMock = mockStatic(ZonedDateTime.class, CALLS_REAL_METHODS);
        zonedDateTimeMock.when(() -> ZonedDateTime.now(ZoneOffset.UTC)).thenReturn(FIXED_NOW);
    }

    @AfterEach
    void tearDown() {
        zonedDateTimeMock.close();
    }

    @Override
    protected Query buildQuery(int fromValue) {
        ZonedDateTime timeMinus48WorkingHours = FIXED_NOW_LONDON.minusDays(2);
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(rangeQuery("data.joDJCreatedDate").lte(timeMinus48WorkingHours.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                        .must(boolQuery().must(matchQuery("state", CaseState.JUDGMENT_REQUESTED.toString())))
                        .must(((JudgementBufferExpiredSearchService) searchService).haveNoOngoingBusinessProcess()));
        return new Query(query, List.of("reference"), fromValue, true);
    }
}
