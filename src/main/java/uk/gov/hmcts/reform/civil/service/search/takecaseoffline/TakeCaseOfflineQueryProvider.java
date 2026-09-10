package uk.gov.hmcts.reform.civil.service.search.takecaseoffline;

import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.search.PageToken;
import uk.gov.hmcts.reform.civil.model.search.PaginatedQuery;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.search.common.CommonQueryConstructs;
import uk.gov.hmcts.reform.civil.service.search.common.PaginatedQueryProvider;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;

@Component
@RequiredArgsConstructor
public class TakeCaseOfflineQueryProvider implements PaginatedQueryProvider {

    private final FeatureToggleService featureToggleService;
    private final CommonQueryConstructs commonQueryConstructs;
    private final Time time;

    @Override
    public PaginatedQuery getPaginatedQuery(PageToken pageToken, int pageSize) {
        String timeNow = ZonedDateTime.of(time.now(), ZoneOffset.UTC).toString();
        return new PaginatedQuery(
            buildQuery(timeNow),
            List.of("reference"),
            0,
            pageToken,
            pageSize
        );
    }

    public BoolQueryBuilder buildQuery(String timeNow) {
        BoolQueryBuilder queryBuilder = boolQuery().minimumShouldMatch(1);

        if (featureToggleService.isWelshEnabledForMainCase()) {
            queryBuilder.should(
                boolQuery()
                    .must(rangeQuery("data.applicant1ResponseDeadline").lt(timeNow))
                    .must(commonQueryConstructs.beState(AWAITING_APPLICANT_INTENTION))
                    .mustNot(matchQuery("data.isMintiLipCase", "Yes"))
                    .mustNot(existsQuery("data.applicant1ResponseDate")));
        } else {
            queryBuilder.should(
                boolQuery()
                    .must(rangeQuery("data.applicant1ResponseDeadline").lt(timeNow))
                    .must(commonQueryConstructs.beState(AWAITING_APPLICANT_INTENTION))
                    .mustNot(matchQuery("data.isMintiLipCase", "Yes")));
        }

        queryBuilder.should(
            boolQuery()
                .must(rangeQuery("data.addLegalRepDeadlineRes1").lt(timeNow))
                .must(commonQueryConstructs.beState(AWAITING_RESPONDENT_ACKNOWLEDGEMENT)));

        queryBuilder.should(
            boolQuery()
                .must(rangeQuery("data.addLegalRepDeadlineRes2").lt(timeNow))
                .must(commonQueryConstructs.beState(AWAITING_RESPONDENT_ACKNOWLEDGEMENT)));

        return queryBuilder;
    }
}
