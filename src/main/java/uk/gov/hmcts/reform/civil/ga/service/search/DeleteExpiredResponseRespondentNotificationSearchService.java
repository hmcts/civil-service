package uk.gov.hmcts.reform.civil.ga.service.search;

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;

@Service
public class DeleteExpiredResponseRespondentNotificationSearchService extends GaElasticSearchService {

    public static final LocalTime END_OF_BUSINESS_DAY = LocalTime.of(16, 0, 0);

    public DeleteExpiredResponseRespondentNotificationSearchService(GaCoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    public Set<CaseDetails> getApplications() {

        SearchResult searchResult = coreCaseDataService
            .searchGeneralApplication(query(START_INDEX));

        int pages = calculatePages(searchResult);
        List<CaseDetails> caseDetails = new ArrayList<>(searchResult.getCases());

        for (int i = 1; i < pages; i++) {
            caseDetails.addAll(coreCaseDataService.searchGeneralApplication(query(i * ES_DEFAULT_SEARCH_LIMIT)).getCases());
        }

        return new HashSet<>(caseDetails);
    }

    public Query query(int startIndex) {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalDateTime.now().toLocalTime();
        LocalDate targetDate = currentTime.isAfter(END_OF_BUSINESS_DAY) ? currentDate.plusDays(1) : currentDate;
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(rangeQuery("data.judicialDecisionRequestMoreInfo.judgeRequestMoreInfoByDate").lt(targetDate
                                                                                              .toString()))
                            .mustNot(matchQuery("data.respondentResponseDeadlineChecked", "Yes"))
                            .must(beState(AWAITING_RESPONDENT_RESPONSE))),
            List.of("reference"),
            startIndex
        );
    }

    @Override
    Query query(final int startIndex, final CaseState caseState) {
        return null;
    }

    @Override
    Query queryForOrderMade(final int startIndex, final CaseState caseState, final GeneralApplicationTypes gaType) {
        return null;
    }

    @Override
    Query queryForBusinessProcessStatus(final int startIndex, final BusinessProcessStatus processStatus) {
        return null;
    }

    private QueryBuilder beState(CaseState caseState) {
        return boolQuery()
            .must(matchQuery("state", caseState.toString()));
    }
}

