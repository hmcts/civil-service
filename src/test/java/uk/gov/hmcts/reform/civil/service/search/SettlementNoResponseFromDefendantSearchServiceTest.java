package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

class SettlementNoResponseFromDefendantSearchServiceTest extends ElasticSearchServiceTest {

    private static final int BUSINESS_DAYS_FROM_NOW = 0;
    public static final LocalTime END_OF_BUSINESS_DAY = LocalTime.of(16, 0, 0);

    @BeforeEach
    void setup() {
        searchService = new SettlementNoResponseFromDefendantSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        String expectedDate = DateUtils.addDaysSkippingWeekends(
                LocalDate.now().minusDays(1), BUSINESS_DAYS_FROM_NOW).atTime(END_OF_BUSINESS_DAY)
            .format(DateTimeFormatter.ISO_DATE);

        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(matchQuery("data.respondent1RespondToSettlementAgreementDeadline", expectedDate))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_APPLICANT_INTENTION"))));
        return new Query(query, List.of("reference"), fromValue);
    }

    @Override
    protected Query buildQueryInMediation(int fromValue, LocalDate date, boolean carmEnabled,
                                          boolean initialSearch,
                                          String searchAfterValue) {
        return null;
    }
}
