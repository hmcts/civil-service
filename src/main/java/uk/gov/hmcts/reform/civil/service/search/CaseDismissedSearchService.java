package uk.gov.hmcts.reform.civil.service.search;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDGMENT_REQUESTED;

@Service
@Slf4j
public class CaseDismissedSearchService extends ElasticSearchService {

    public CaseDismissedSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    @Override
    public Query query(int startIndex, String timeNow) {
        log.info("Call to CaseDismissedSearchService query with index {} and timeNow {}", startIndex, timeNow);
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(rangeQuery("data.claimNotificationDeadline").lt(timeNow))
                            .must(beState(CASE_ISSUED)))
                .should(boolQuery()
                            .must(rangeQuery("data.claimDismissedDeadline").lt(timeNow))
                            .must(boolQuery()
                                      .minimumShouldMatch(1)
                                      .should(beState(AWAITING_RESPONDENT_ACKNOWLEDGEMENT))
                                      .should(beState(JUDGMENT_REQUESTED)))),
            List.of("reference"),
            startIndex
        );
    }

    public BoolQueryBuilder beState(CaseState state) {
        return boolQuery()
            .must(matchQuery("state", state.toString()));
    }
}
