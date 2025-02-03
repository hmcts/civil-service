package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;

@Service
public class TrialReadyCheckSearchService extends ElasticSearchService {

    public TrialReadyCheckSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    public Query query(int startIndex) {
        String allocatedTrackPath = "data.allocatedTrack";
        String responseTrackPath = "data.responseClaimTrack";

        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(rangeQuery("data.hearingDate").lt(LocalDate.now()
                                                                            .atTime(LocalTime.MIN).plusWeeks(3)
                                                                            .toString()))
                            .must(boolQuery()
                                      .minimumShouldMatch(1)
                                      .should(beState(PREPARE_FOR_HEARING_CONDUCT_HEARING))
                                      .should(beState(HEARING_READINESS)))
                            .mustNot(matchQuery(allocatedTrackPath, "SMALL_CLAIM"))
                            .mustNot(matchQuery(responseTrackPath, "SMALL_CLAIM"))
                            .mustNot(matchQuery(allocatedTrackPath, "MULTI_CLAIM"))
                            .mustNot(matchQuery(responseTrackPath, "MULTI_CLAIM"))
                            .mustNot(matchQuery(allocatedTrackPath, "INTERMEDIATE_CLAIM"))
                            .mustNot(matchQuery(responseTrackPath, "INTERMEDIATE_CLAIM"))
                            .mustNot(matchQuery("data.trialReadyChecked", "Yes"))),
            List.of("reference"),
            startIndex
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
