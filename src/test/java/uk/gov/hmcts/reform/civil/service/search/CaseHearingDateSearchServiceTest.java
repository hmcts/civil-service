package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

@SuppressWarnings("unchecked")
class CaseHearingDateSearchServiceTest extends ElasticSearchServiceTest {

    @BeforeEach
    void setup() {
        searchService = new CaseHearingDateSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        LocalDate baseDate = LocalDate.now(ZoneOffset.UTC);
        String targetMaxDateString = DateUtils.addDaysSkippingWeekends(
            baseDate, 10).format(DateTimeFormatter.ISO_DATE);
        String targetMinDateString = DateUtils.addDaysSkippingWeekends(
            baseDate, 3).format(DateTimeFormatter.ISO_DATE);
        BoolQueryBuilder query = boolQuery()
            .mustNot(matchQuery("state", "PREPARE_FOR_HEARING_CONDUCT_HEARING"))
            .must(boolQuery()
                      .minimumShouldMatch(1)
                      .should(rangeQuery("data.hearingDate").lte(targetMaxDateString).gte(
                          targetMinDateString))
            );
        return new Query(query, List.of("reference"), fromValue, true);
    }
}
