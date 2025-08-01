package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

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
                        .must(boolQuery().must(matchQuery("state", "AWAITING_APPLICANT_INTENTION")))
                        .mustNot(matchQuery("data.isMintiLipCase", "Yes")))
            .should(boolQuery()
                        .must(rangeQuery("data.addLegalRepDeadlineRes1").lt("now"))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_RESPONDENT_ACKNOWLEDGEMENT"))))
            .should(boolQuery()
                        .must(rangeQuery("data.addLegalRepDeadlineRes2").lt("now"))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_RESPONDENT_ACKNOWLEDGEMENT"))));

        return new Query(query, List.of("reference"), fromValue);
    }
}
