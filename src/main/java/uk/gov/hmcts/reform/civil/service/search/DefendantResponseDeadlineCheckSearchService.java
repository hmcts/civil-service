package uk.gov.hmcts.reform.civil.service.search;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

@Service
@Slf4j
public class DefendantResponseDeadlineCheckSearchService extends ElasticSearchService {

    private final FeatureToggleService featureToggleService;

    public DefendantResponseDeadlineCheckSearchService(CoreCaseDataService coreCaseDataService,
                                                       FeatureToggleService featureToggleService) {
        super(coreCaseDataService);
        this.featureToggleService = featureToggleService;
    }

    @Override
    public Query query(int startIndex, String timeNow) {
        log.info("Call to DefendantResponseDeadlineCheckSearchService query with index {} and timeNow {}", startIndex, timeNow);
        if (featureToggleService.isWelshEnabledForMainCase()) {
            return new Query(
                boolQuery()
                    .minimumShouldMatch(1)
                    .should(boolQuery()
                                .must(rangeQuery("data.respondent1ResponseDeadline").lt(timeNow))
                                .mustNot(matchQuery("data.respondent1ResponseDeadlineChecked", "Yes"))
                                .mustNot(existsQuery("data.respondent1ResponseDate"))
                                .must(beState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT))
                                .must(haveNoOngoingBusinessProcess())
                    ),
                List.of("reference"),
                startIndex,
                true
            );
        }
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(rangeQuery("data.respondent1ResponseDeadline").lt(timeNow))
                            .mustNot(matchQuery("data.respondent1ResponseDeadlineChecked", "Yes"))
                            .must(beState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT))
                            .must(haveNoOngoingBusinessProcess())
                ),
            List.of("reference"),
            startIndex,
            true
        );
    }

    public BoolQueryBuilder beState(CaseState state) {
        return boolQuery()
            .must(matchQuery("state", state.toString()));
    }

    public BoolQueryBuilder haveNoOngoingBusinessProcess() {
        return boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery().mustNot(existsQuery("data.businessProcess")))
            .should(boolQuery().mustNot(existsQuery("data.businessProcess.status")))
            .should(boolQuery().must(matchQuery("data.businessProcess.status", "FINISHED")));
    }
}
