package uk.gov.hmcts.reform.civil.service.search;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;

@Service
@Slf4j
public class TakeCaseOfflineSearchService extends ElasticSearchService {

    public TakeCaseOfflineSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    private static final String DATE_FORMATTER = "yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd";

    public Query query(int startIndex) {
        Query query = new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                    .must(rangeQuery("data.applicant1ResponseDeadline").lt("now").format(DATE_FORMATTER))
                    .must(beState(AWAITING_APPLICANT_INTENTION)))
                .should(boolQuery()
                    .must(rangeQuery("data.addLegalRepDeadlineRes1").lt("now").format(DATE_FORMATTER))
                    .must(beState(AWAITING_RESPONDENT_ACKNOWLEDGEMENT)))
                .should(boolQuery()
                    .must(rangeQuery("data.addLegalRepDeadlineRes2").lt("now").format(DATE_FORMATTER))
                    .must(beState(AWAITING_RESPONDENT_ACKNOWLEDGEMENT))),
            List.of("reference"),
            startIndex
        );
        log.info("Query to take case offline {}", query);
        return query;
    }

    @Override
    Query queryInMediationCases(int startIndex, LocalDate claimMovedDate, boolean carmEnabled) {
        return null;
    }

    private QueryBuilder beState(CaseState caseState) {
        return boolQuery()
            .must(matchQuery("state", caseState.toString()));
    }
}
