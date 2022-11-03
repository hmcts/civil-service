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
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
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
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(rangeQuery("data.caseDocumentUploadDate").lt("now").gt(
                                "now-1d"))
                            .must(beState(CASE_PROGRESSION)))
                .should(boolQuery()
                            .must(rangeQuery("data.caseDocumentUploadDate").lt("now").gt(
                                "now-1d"))
                            .must(beState(HEARING_READINESS)))
                .should(boolQuery()
                            .must(rangeQuery("data.caseDocumentUploadDate").lt("now").gt(
                                "now-1d"))
                            .must(beState(PREPARE_FOR_HEARING_CONDUCT_HEARING))),
            List.of("reference"),
            startIndex
        );
    }

    public BoolQueryBuilder beState(CaseState state) {
        return boolQuery()
            .must(matchQuery("state", state.toString()));
    }
}
