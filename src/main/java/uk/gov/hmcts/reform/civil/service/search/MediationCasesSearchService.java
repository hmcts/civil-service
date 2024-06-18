package uk.gov.hmcts.reform.civil.service.search;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.CarmDateConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.util.Collections.emptyList;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;

@Service
@Slf4j
public class MediationCasesSearchService extends ElasticSearchService {

    private final CarmDateConfiguration carmDateConfiguration;

    public MediationCasesSearchService(CoreCaseDataService coreCaseDataService, CarmDateConfiguration carmDateConfiguration) {
        super(coreCaseDataService);
        this.carmDateConfiguration = carmDateConfiguration;
    }

    private QueryBuilder beState(CaseState caseState) {
        return boolQuery()
            .must(matchQuery("state", caseState.toString()));
    }

    private QueryBuilder submittedDate(boolean carmEnabled) {
        log.info(carmDateConfiguration.getCarmDate() + "");
        if (carmEnabled) {
            return boolQuery()
                .must(rangeQuery("data.submittedDate").gte(carmDateConfiguration.getCarmDate()));
        } else {
            return boolQuery()
                .must(rangeQuery("data.submittedDate").lt(carmDateConfiguration.getCarmDate()));
        }
    }

    @Override
    Query query(int startIndex) {
        return null;
    }

    @Override
    Query queryInMediationCases(int startIndex, LocalDate claimMovedDate, boolean carmEnabled) {
        String targetDateString =
            claimMovedDate.format(DateTimeFormatter.ISO_DATE);
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(beState(IN_MEDIATION))
                            .must(submittedDate(carmEnabled))
                            .must(matchQuery("data.claimMovedToMediationOn", targetDateString))),
            emptyList(),
            startIndex
        );
    }
}
