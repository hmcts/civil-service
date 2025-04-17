package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.util.Collections.emptyList;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;

@Service
public class MediationCasesSearchService extends ElasticSearchService {

    private static final LocalDateTime CARM_DATE = LocalDateTime.of(2024, 11, 5,
                                                                    7, 28, 35
    );

    public MediationCasesSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    private QueryBuilder beState(CaseState caseState) {
        return boolQuery()
            .must(matchQuery("state", caseState.toString()));
    }

    private QueryBuilder submittedDate(boolean carmEnabled) {
        if (carmEnabled) {
            return boolQuery()
                .must(rangeQuery("data.submittedDate").gte(CARM_DATE));
        } else {
            return boolQuery()
                .must(rangeQuery("data.submittedDate").lt(CARM_DATE));
        }
    }

    @Override
    Query query(int startIndex) {
        return null;
    }

    @Override
    Query queryInMediationCases(int startIndex, LocalDate claimMovedDate, boolean carmEnabled, boolean initialSearch,
                                    String searchAfterValue) {
        String targetDateString =
            claimMovedDate.format(DateTimeFormatter.ISO_DATE);
        if (carmEnabled) {
            return new Query(
                boolQuery()
                    .must(matchAllQuery())
                    .must(beState(IN_MEDIATION))
                    .must(submittedDate(carmEnabled))
                    .must(matchQuery("data.claimMovedToMediationOn", targetDateString))
                    .mustNot(matchQuery("data.mediationJsonEmailSent", "Yes")),
                emptyList(),
                startIndex,
                initialSearch,
                searchAfterValue
            );
        }
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(beState(IN_MEDIATION))
                            .must(submittedDate(carmEnabled))
                            .must(matchQuery("data.claimMovedToMediationOn", targetDateString)))
                .mustNot(matchQuery("data.mediationJsonEmailSent", "Yes")),
            emptyList(),
            startIndex
        );
    }
}
