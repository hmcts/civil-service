package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

@Service
public class BundleCreationTriggerService extends ElasticSearchService {

    private static final int BUNDLE_CREATION_TIME_RANGE = 10;

    public BundleCreationTriggerService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    public Query query(int startIndex) {
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(rangeQuery("data.hearingDate").lte(LocalDate.now().plusDays(BUNDLE_CREATION_TIME_RANGE)))
                            .must(beState(CaseState.HEARING_READINESS)))
                .should(boolQuery()
                            .must(rangeQuery("data.hearingDate").lte(LocalDate.now().plusDays(BUNDLE_CREATION_TIME_RANGE)))
                            .must(beState(CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING))
                )
                .mustNot(matchQuery("data.allocatedTrack", "MULTI_CLAIM"))
                .mustNot(matchQuery("data.allocatedTrack", "INTERMEDIATE_CLAIM"))
                .mustNot(matchQuery("data.responseClaimTrack", "MULTI_CLAIM"))
                .mustNot(matchQuery("data.responseClaimTrack", "INTERMEDIATE_CLAIM")),
            List.of("reference"), startIndex
        );
    }

    @Override
    Query queryInMediationCases(int startIndex, LocalDate claimMovedDate, boolean carmEnabled, boolean initialSearch,
                                String searchAfterValue) {
        return null;
    }

    private QueryBuilder beState(CaseState caseState) {
        return boolQuery()
            .must(matchQuery("state", caseState.toString()));
    }
}
