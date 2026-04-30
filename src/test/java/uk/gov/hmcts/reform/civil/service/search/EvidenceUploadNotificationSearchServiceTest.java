package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.time.ZonedDateTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

class EvidenceUploadNotificationSearchServiceTest extends ElasticSearchServiceTest {

    private String capturedTimeNow;

    @BeforeEach
    void setup() {
        searchService = new EvidenceUploadNotificationSearchService(coreCaseDataService) {
            @Override
            public Query query(int startIndex, String timeNow) {
                capturedTimeNow = timeNow;
                return super.query(startIndex, timeNow);
            }
        };
    }

    @Override
    protected Query buildQuery(int fromValue) {
        ZonedDateTime now = ZonedDateTime.parse(capturedTimeNow);
        ZonedDateTime sevenDaysAgo = now.minusDays(7);
        BoolQueryBuilder query = boolQuery()
            .must(boolQuery()
                      .minimumShouldMatch(1)
                      .should(boolQuery().must(matchQuery("state", "PREPARE_FOR_HEARING_CONDUCT_HEARING")))
                      .should(boolQuery().must(matchQuery("state", "HEARING_READINESS")))
                      .should(boolQuery().must(matchQuery("state", "DECISION_OUTCOME")))
                      .should(boolQuery().must(matchQuery("state", "All_FINAL_ORDERS_ISSUED")))
                      .should(boolQuery().must(matchQuery("state", "CASE_PROGRESSION"))))
            .mustNot(matchQuery("data.evidenceUploadNotificationSent", "Yes"))
            .must(boolQuery()
                      .minimumShouldMatch(1)
                      .should(rangeQuery("data.caseDocumentUploadDate").lt(now).gt(
                          sevenDaysAgo))
                      .should(rangeQuery("data.caseDocumentUploadDateRes").lt(now).gt(
                          sevenDaysAgo))
                      );
        return new Query(query, List.of("reference"), fromValue);
    }
}
