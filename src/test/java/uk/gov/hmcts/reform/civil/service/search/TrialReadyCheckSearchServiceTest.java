package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.enums.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

class TrialReadyCheckSearchServiceTest extends ElasticSearchServiceTest {

    @BeforeEach
    void setup() {
        searchService = new TrialReadySearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                .must(rangeQuery("data.hearingDate").lt(LocalDate.now().plusWeeks(6)))
                .must(boolQuery().must(matchQuery("state", "PREPARE_FOR_HEARING_CONDUCT_HEARING"))))
                .mustNot(matchQuery("data.listingOrRelisting", ListingOrRelisting.RELISTING))
                .mustNot(matchQuery("data.trialReadyNotified", YesOrNo.YES));

        return new Query(query, List.of("reference"), fromValue);
    }
}
