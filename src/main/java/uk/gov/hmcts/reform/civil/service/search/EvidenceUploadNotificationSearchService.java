package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.All_FINAL_ORDERS_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.DECISION_OUTCOME;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;

@Service
public class EvidenceUploadNotificationSearchService extends ElasticSearchService {

    public EvidenceUploadNotificationSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    public Query query(int startIndex) {

        return new Query(
            boolQuery()
                .must(boolQuery()
                          .minimumShouldMatch(1)
                          .should(beState(PREPARE_FOR_HEARING_CONDUCT_HEARING))
                          .should(beState(HEARING_READINESS))
                          .should(beState(DECISION_OUTCOME))
                          .should(beState(All_FINAL_ORDERS_ISSUED))
                          .should(beState(CASE_PROGRESSION)))
                .mustNot(matchQuery("data.evidenceUploadNotificationSent", "Yes"))
                .must(boolQuery()
                          .minimumShouldMatch(1)
                          .should(rangeQuery("data.caseDocumentUploadDate").lt("now/d").gt(
                              "now-7d"))
                          .should(rangeQuery("data.caseDocumentUploadDateRes").lt("now/d").gt(
                              "now-7d"))
                          ),
            List.of("reference"),
            startIndex
        );
    }

    public BoolQueryBuilder beState(CaseState state) {
        return boolQuery()
            .must(matchQuery("state", state.toString()));
    }
}
