package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

class TrialReadyCheckSearchServiceTest extends ElasticSearchServiceTest {

    private String capturedTimeNow;

    @BeforeEach
    void setup() {
        searchService = new TrialReadyCheckSearchService(coreCaseDataService) {
            @Override
            public Query query(int startIndex, String timeNow) {
                capturedTimeNow = timeNow;
                return super.query(startIndex, timeNow);
            }
        };
    }

    @Override
    protected Query buildQuery(int fromValue) {
        ZonedDateTime now = (capturedTimeNow != null
            ? ZonedDateTime.parse(capturedTimeNow)
            : ZonedDateTime.now(ZoneOffset.UTC))
            .withZoneSameInstant(ZoneOffset.UTC);
        LocalDate baseDate = now.toLocalDate();
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                .must(rangeQuery("data.hearingDate").lt(baseDate.atTime(LocalTime.MIN).plusWeeks(3).atZone(ZoneOffset.UTC).toString()))
                .must(boolQuery()
                          .minimumShouldMatch(1)
                          .should(boolQuery().must(matchQuery("state", "PREPARE_FOR_HEARING_CONDUCT_HEARING")))
                          .should(boolQuery().must(matchQuery("state", "HEARING_READINESS"))))
                .mustNot(matchQuery("data.allocatedTrack", "SMALL_CLAIM"))
                .mustNot(matchQuery("data.responseClaimTrack", "SMALL_CLAIM"))
                .mustNot(matchQuery("data.allocatedTrack", "MULTI_CLAIM"))
                .mustNot(matchQuery("data.responseClaimTrack", "MULTI_CLAIM"))
                .mustNot(matchQuery("data.allocatedTrack", "INTERMEDIATE_CLAIM"))
                .mustNot(matchQuery("data.responseClaimTrack", "INTERMEDIATE_CLAIM"))
                .mustNot(matchQuery("data.trialReadyChecked", "Yes")));

        return new Query(query, List.of("reference"), fromValue);
    }
}
