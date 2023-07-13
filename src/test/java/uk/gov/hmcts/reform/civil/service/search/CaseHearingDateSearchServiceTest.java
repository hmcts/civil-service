package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.mockito.Mockito.mockStatic;

@SuppressWarnings("unchecked")
class CaseHearingDateSearchServiceTest extends ElasticSearchServiceTest {

    private static final LocalDate CURRENT_DATE = LocalDate.of(2023, 7, 10);
    private static MockedStatic currentDateMock;

    @BeforeAll
    static void setupSuite() {
        currentDateMock = mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
        currentDateMock.when(LocalDate::now).thenReturn(CURRENT_DATE);
    }

    @AfterAll
    static void tearDown() {
        currentDateMock.reset();
    }

    @BeforeEach
    void setup() {
        searchService = new CaseHearingDateSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        String targetDate = LocalDate.of(2023, 07, 24).format(DateTimeFormatter.ISO_DATE);
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(matchQuery("data.hearingDate", targetDate)));
        return new Query(query, List.of("reference"), fromValue);
    }

}
