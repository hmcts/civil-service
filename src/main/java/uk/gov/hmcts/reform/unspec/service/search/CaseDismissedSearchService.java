package uk.gov.hmcts.reform.unspec.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.enums.CaseState;
import uk.gov.hmcts.reform.unspec.model.search.Query;
import uk.gov.hmcts.reform.unspec.service.CoreCaseDataService;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.unspec.enums.CaseState.AWAITING_CASE_DETAILS_NOTIFICATION;
import static uk.gov.hmcts.reform.unspec.enums.CaseState.CREATED;

@Service
public class CaseDismissedSearchService extends ElasticSearchService {

    public CaseDismissedSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    //TODO: claimDismissedDeadline is not yet set anywhere. Date is 6 months
    // of no activity in state view and respond to defence which is currently AWAITING_CLAIMANT_INTENTION.
    public Query query(int startIndex) {
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(rangeQuery("data.claimDetailsNotificationDeadline").lt("now"))
                            .must(beState(AWAITING_CASE_DETAILS_NOTIFICATION)))
                .should(boolQuery()
                            .must(rangeQuery("data.claimDismissedDeadline").lt("now"))
                            .must(beState(CREATED))),
            List.of("reference"),
            startIndex
        );
    }

    public BoolQueryBuilder beState(CaseState state) {
        return boolQuery()
            .must(matchQuery("state", state.toString()));
    }
}
