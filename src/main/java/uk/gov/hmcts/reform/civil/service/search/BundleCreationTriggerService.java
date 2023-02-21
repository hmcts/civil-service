package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@Service
public class BundleCreationTriggerService extends ElasticSearchService {

    private static final int BUNDLE_CREATION_TIME_RANGE = 3;

    public BundleCreationTriggerService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    public Query query(int startIndex) {
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(matchQuery("data.hearingDate", LocalDate.now().plusWeeks(BUNDLE_CREATION_TIME_RANGE)))
                            .must(beState(CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING))
                            .mustNot(matchQuery("data.bundleCreationNotified", YesOrNo.YES))
                ), List.of("reference"), startIndex
        );
    }

    private QueryBuilder beState(CaseState caseState) {
        return boolQuery()
            .must(matchQuery("state", caseState.toString()));
    }
}
