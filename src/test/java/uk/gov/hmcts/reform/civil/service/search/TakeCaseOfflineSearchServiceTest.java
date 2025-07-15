package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
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
        String allocatedTrackPath = "data.allocatedTrack";
        String responseTrackPath = "data.responseClaimTrack";
        String isLipCase = "data.isLipOnCase";

        QueryBuilder lipOnCaseMultiOrIntermediateTrack = boolQuery()
            .must(termQuery(isLipCase, YesOrNo.YES.name()))
            .must(
                boolQuery()
                    .should(matchQuery(allocatedTrackPath, "MULTI_CLAIM"))
                    .should(matchQuery(responseTrackPath, "MULTI_CLAIM"))
                    .should(matchQuery(allocatedTrackPath, "INTERMEDIATE_CLAIM"))
                    .should(matchQuery(responseTrackPath, "INTERMEDIATE_CLAIM"))
            );

        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(rangeQuery("data.applicant1ResponseDeadline").lt("now"))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_APPLICANT_INTENTION")))
                        .mustNot(lipOnCaseMultiOrIntermediateTrack))
            .should(boolQuery()
                        .must(rangeQuery("data.addLegalRepDeadlineRes1").lt("now"))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_RESPONDENT_ACKNOWLEDGEMENT"))))
            .should(boolQuery()
                        .must(rangeQuery("data.addLegalRepDeadlineRes2").lt("now"))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_RESPONDENT_ACKNOWLEDGEMENT"))));

        return new Query(query, List.of("reference"), fromValue);
    }
}
