package uk.gov.hmcts.reform.civil.service.search;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_STAYED;

@Service
public class ManageStayUpdateRequestedSearchService extends ElasticSearchService {

    private static final int DAYS = 7;

    public ManageStayUpdateRequestedSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    public Query query(int startIndex) {
        return new Query(
                boolQuery()
                        .minimumShouldMatch(1)
                        .should(boolQuery()
                                    .must(rangeQuery("data.manageStayUpdateRequestDate").lt("now-7d/d"))
                                    .must(matchQuery("state", CASE_STAYED.toString()))
                        ),
                List.of("reference"),
                startIndex
        );
    }

    @Override
    Query queryInMediationCases(int startIndex, LocalDate claimMovedDate, boolean carmEnabled,
                                boolean initialSearch,
                                String searchAfterValue) {
        return null;
    }
}
