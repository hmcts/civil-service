package uk.gov.hmcts.reform.civil.service.search;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;

@Service
public class TakeCaseOfflineSearchService extends ElasticSearchService {

    public TakeCaseOfflineSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    public Query query(int startIndex) {
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(rangeQuery("data.applicant1ResponseDeadline").lt("now"))
                            .must(beState(AWAITING_APPLICANT_INTENTION)))
                .should(boolQuery()
                            .must(termQuery("data.addRespondent2", "Yes"))
                            .must(beState(AWAITING_RESPONDENT_ACKNOWLEDGEMENT))
                            .should(boolQuery()
                                        .minimumShouldMatch(1)
                                        .should(rangeQuery("data.respondent1ResponseDeadline").lt("now"))
                                        .should(rangeQuery("data.respondent2ResponseDeadline").lt("now")))
                            .should(boolQuery()
                                        .minimumShouldMatch(1)
                                        .should(existsQuery("data.respondent1ResponseDate"))
                                        .should(existsQuery("data.respondent2ResponseDate")))
                ),
            List.of("reference"),
            startIndex
        );
    }
}
