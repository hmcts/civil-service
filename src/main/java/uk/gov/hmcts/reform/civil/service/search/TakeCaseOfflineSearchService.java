package uk.gov.hmcts.reform.civil.service.search;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;

@Slf4j
@Service
public class TakeCaseOfflineSearchService extends ElasticSearchService {

    public TakeCaseOfflineSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    public Query query(int startIndex) {

        String allocatedTrackPath = "data.allocatedTrack";
        String responseTrackPath = "data.responseClaimTrack";
        String isLipCase = "data.isLipOnCase";

        // if isLipCase is true and track is multi/intermediate do not take offline, as it will be done manually
        QueryBuilder lipOnCaseMultiOrIntermediateTrack = boolQuery()
            .must(termQuery(isLipCase, YesOrNo.YES.name()))
            .must(
                boolQuery()
                    .should(matchQuery(allocatedTrackPath, "MULTI_CLAIM"))
                    .should(matchQuery(responseTrackPath, "MULTI_CLAIM"))
                    .should(matchQuery(allocatedTrackPath, "INTERMEDIATE_CLAIM"))
                    .should(matchQuery(responseTrackPath, "INTERMEDIATE_CLAIM"))
            );

        Query query = new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(rangeQuery("data.applicant1ResponseDeadline").lt("now"))
                            .must(beState(AWAITING_APPLICANT_INTENTION))
                            .mustNot(lipOnCaseMultiOrIntermediateTrack))
                .should(boolQuery()
                            .must(rangeQuery("data.addLegalRepDeadlineRes1").lt("now"))
                            .must(beState(AWAITING_RESPONDENT_ACKNOWLEDGEMENT)))
                .should(boolQuery()
                            .must(rangeQuery("data.addLegalRepDeadlineRes2").lt("now"))
                            .must(beState(AWAITING_RESPONDENT_ACKNOWLEDGEMENT))),
            List.of("reference"),
            startIndex
        );
        log.info("Take Case Offline query: {}", query);
        return query;
    }

    private QueryBuilder beState(CaseState caseState) {
        return boolQuery()
            .must(matchQuery("state", caseState.toString()));
    }
}
