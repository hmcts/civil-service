package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

class EvidenceUploadNotificationSearchServiceTest extends ElasticSearchServiceTest {

    @BeforeEach
    void setup() {
        searchService = new EvidenceUploadNotificationSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        BoolQueryBuilder query = boolQuery()
            .must(boolQuery()
                      .minimumShouldMatch(1)
                      .should(boolQuery().must(matchQuery("state", "PREPARE_FOR_HEARING_CONDUCT_HEARING")))
                      .should(boolQuery().must(matchQuery("state", "HEARING_READINESS")))
                      .should(boolQuery().must(matchQuery("state", "CASE_PROGRESSION"))))
            .must(boolQuery()
                      .minimumShouldMatch(1)
                      .should(rangeQuery("data.caseDocumentUploadDate").lt("now").gt(
                          "now-1d"))
                      .should(rangeQuery("data.caseDocumentUploadDateRes").lt("now").gt(
                          "now-1d"))
                      );
        return new Query(query, List.of("reference"), fromValue);
    }
}
