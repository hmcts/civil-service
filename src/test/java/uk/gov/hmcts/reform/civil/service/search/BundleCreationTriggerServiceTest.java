package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

class BundleCreationTriggerServiceTest extends ElasticSearchServiceTest {

    @BeforeEach
    void setup() {
        searchService = new BundleCreationTriggerService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(rangeQuery("data.hearingDate").lte(LocalDate.now().plusDays(10)))
                        .must(boolQuery().must(matchQuery("state", "HEARING_READINESS"))))
            .should(boolQuery()
                        .must(rangeQuery("data.hearingDate").lte(LocalDate.now().plusDays(10)))
                        .must(boolQuery().must(matchQuery("state", "PREPARE_FOR_HEARING_CONDUCT_HEARING"))))
            .mustNot(matchQuery("data.allocatedTrack", "MULTI_CLAIM"))
            .mustNot(matchQuery("data.allocatedTrack", "INTERMEDIATE_CLAIM"))
            .mustNot(matchQuery("data.responseClaimTrack", "MULTI_CLAIM"))
            .mustNot(matchQuery("data.responseClaimTrack", "INTERMEDIATE_CLAIM"));
        return new Query(query, List.of("reference"), fromValue);
    }
}
