package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;

@Service
public class FullAdmitPayImmediatelyNoPaymentFromDefendantSearchService extends ElasticSearchService {

    public FullAdmitPayImmediatelyNoPaymentFromDefendantSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    public Query query(int startIndex) {

        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .mustNot(matchQuery("data.fullAdmitNoPaymentSchedulerProcessed", "Yes"))
                            .must(rangeQuery(
                                "data.respondToClaimAdmitPartLRspec.whenWillThisAmountBePaid")
                                      .gte("now-8d/d").lte("now-1d/d"))
                            .must(matchQuery("data.respondent1ClaimResponseTypeForSpec",
                        RespondentResponseType.FULL_ADMISSION
                    ))
                            .must(beState(AWAITING_APPLICANT_INTENTION))
                ),
            List.of("reference"),
            startIndex
        );
    }

    private QueryBuilder beState(CaseState caseState) {
        return boolQuery()
            .must(matchQuery("state", caseState.toString()));
    }
}
