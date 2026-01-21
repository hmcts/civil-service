package uk.gov.hmcts.reform.civil.ga.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.UNLESS_ORDER;

@Service
public class CaseStateSearchService extends GaElasticSearchService {

    public CaseStateSearchService(GaCoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    @Override
    public Query query(int startIndex, CaseState caseState) {

        return new Query(
            matchQuery("state", caseState.toString()),
            emptyList(),
            startIndex
        );
    }

    @Override
    Query queryForOrderMade(int startIndex, CaseState caseState, GeneralApplicationTypes gaType) {
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
    public Query queryForBusinessProcessStatus(int startIndex, BusinessProcessStatus processStatus) {
        return new Query(
            boolQuery().must(matchQuery("data.businessProcess.status", processStatus)),
            List.of(),
            startIndex
        );
    }
}
