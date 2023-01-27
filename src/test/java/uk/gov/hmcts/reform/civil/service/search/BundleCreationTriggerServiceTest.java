package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

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
                        .must(matchQuery("data.hearingDate", LocalDate.now().plusWeeks(3)))
                        .must(boolQuery().must(matchQuery("state", "HEARING_READINESS"))));
        return new Query(query, List.of("reference"), fromValue);
    }
}
