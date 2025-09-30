package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.UNLESS_ORDER;

public class CaseStateSearchServiceTest extends ElasticSearchGeneralApplicationServiceTest {

    @BeforeEach
    void setup() {
        searchService = new CaseStateSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue, CaseState caseState) {
        return new Query(
                matchQuery("state", caseState.toString()),
                emptyList(),
                fromValue
        );
    }

    @Override
    protected Query queryForOrderMade(int startIndex, CaseState caseState, GeneralApplicationTypes gaType) {
        MatchQueryBuilder queryCaseState = QueryBuilders.matchQuery("state", caseState.toString());
        MatchQueryBuilder queryGaType = QueryBuilders.matchQuery("data.generalAppType.types", gaType);
        MatchQueryBuilder consentOrder = QueryBuilders
                .matchQuery("data.approveConsentOrder.isOrderProcessedByStayScheduler", "No");
        MatchQueryBuilder queryOrderProcessStatus = gaType.equals(UNLESS_ORDER)
                ? QueryBuilders
                .matchQuery("data.judicialDecisionMakeOrder.isOrderProcessedByUnlessScheduler", "No")
                : QueryBuilders
                .matchQuery("data.judicialDecisionMakeOrder.isOrderProcessedByStayScheduler", "No");

        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(queryCaseState)
                .must(queryGaType)
                .should(queryOrderProcessStatus)
                .should(consentOrder)
                .minimumShouldMatch(1);

        return new Query(
                query,
                emptyList(),
                startIndex
        );
    }

    @Override
    protected Query queryForBusinessProcessStatus(int startIndex, BusinessProcessStatus processStatus) {
        return new Query(
                boolQuery().must(matchQuery("data.businessProcess.status", processStatus)),
                List.of(),
                startIndex
        );
    }
}
