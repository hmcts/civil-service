package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

class CaseHearingDateSearchServiceTest extends ElasticSearchServiceTest {

    @BeforeEach
    void setup() {
        searchService = new CaseHearingDateSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        Clock clock = Clock.fixed(Instant.parse("2023-07-10T10:15:30Z"), ZoneId.of("UTC"));
        Instant.now(clock);

        String targetDate = LocalDate.of(2023, 07, 26).format(DateTimeFormatter.ISO_DATE);

        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(matchQuery("data.hearingDate", targetDate)));
        return new Query(query, List.of("reference"), fromValue);
    }

}
