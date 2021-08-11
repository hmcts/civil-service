package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

class TakeCaseOfflineSearchServiceTest extends ElasticSearchServiceTest {

    @BeforeEach
    void setup() {
        searchService = new TakeCaseOfflineSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(rangeQuery("data.applicant1ResponseDeadline").lt("now"))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_APPLICANT_INTENTION"))))
            .should(boolQuery()
                        .must(termQuery("data.addRespondent2", "Yes"))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_RESPONDENT_ACKNOWLEDGEMENT")))
                        .should(boolQuery()
                                    .minimumShouldMatch(1)
                                    .should(rangeQuery("data.respondent1ResponseDeadline").lt("now"))
                                    .should(rangeQuery("data.respondent2ResponseDeadline").lt("now")))
                        .should(boolQuery()
                                    .minimumShouldMatch(1)
                                    .should(existsQuery("data.respondent1ResponseDate"))
                                    .should(existsQuery("data.respondent2ResponseDate"))));

        return new Query(query, List.of("reference"), fromValue);
    }
}
