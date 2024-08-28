package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;

@Service
public class HearingFeeDueSearchService extends ElasticSearchService {

    private final FeatureToggleService featureToggleService;

    public HearingFeeDueSearchService(CoreCaseDataService coreCaseDataService, FeatureToggleService featureToggleService) {
        super(coreCaseDataService);
        this.featureToggleService = featureToggleService;
    }

    public Query query(int startIndex) {
        if (featureToggleService.isMintiEnabled()) {
            return new Query(
                boolQuery()
                    .minimumShouldMatch(1)
                    .should(boolQuery()
                                .must(beState(HEARING_READINESS))),
                List.of("reference"),
                startIndex
            );
        } else {
            return new Query(
                boolQuery()
                    .minimumShouldMatch(1)
                    .should(boolQuery()
                                .must(rangeQuery("data.hearingDueDate").lt(LocalDate.now()
                                                                               .atTime(LocalTime.MIN)
                                                                               .toString()))
                                .must(beState(HEARING_READINESS))),
                List.of("reference"),
                startIndex
            );
        }
    }

    @Override
    Query queryInMediationCases(int startIndex, LocalDate claimMovedDate, boolean carmEnabled) {
        return null;
    }

    private QueryBuilder beState(CaseState caseState) {
        return boolQuery()
            .must(matchQuery("state", caseState.toString()));
    }
}

