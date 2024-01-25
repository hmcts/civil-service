package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.util.Collections.emptyList;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;

@Service
public class CaseStateSearchService extends ElasticSearchService {

    public CaseStateSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    private QueryBuilder beState(CaseState caseState) {
        return boolQuery()
            .must(matchQuery("state", caseState.toString()));
    }

    @Override
    Query query(int startIndex) {
        return null;
    }

    @Override
    Query queryInMediationCases(int startIndex, LocalDate claimMovedDate) {
        String targetDateString =
            claimMovedDate.format(DateTimeFormatter.ISO_DATE);
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(beState(IN_MEDIATION))
                            .must(matchQuery("data.claimMovedToMediationOn", targetDateString))),
            emptyList(),
            startIndex
        );
    }
}
