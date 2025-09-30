package uk.gov.hmcts.reform.civil.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.math.RoundingMode.UP;

@RequiredArgsConstructor
@Slf4j
public abstract class ElasticSearchService {

    protected static final int START_INDEX = 0;
    protected static final int ES_DEFAULT_SEARCH_LIMIT = 10;
    protected final CoreCaseDataService coreCaseDataService;

    public Set<CaseDetails> getCases() {
        SearchResult searchResult = coreCaseDataService.searchCases(query(START_INDEX));
        int pages = calculatePages(searchResult);
        Set<CaseDetails> caseDetails = new HashSet<>(searchResult.getCases());

        for (int i = 1; i < pages; i++) {
            SearchResult result = coreCaseDataService.searchCases(query(i * ES_DEFAULT_SEARCH_LIMIT));
            caseDetails.addAll(result.getCases());
        }

        List<Long> ids = caseDetails.stream().map(CaseDetails::getId).sorted().toList();
        log.info("Found {} case(s) with ids {}", ids.size(), ids);

        return caseDetails;
    }

    abstract Query query(int startIndex);

    public Query query(int startIndex, CaseState caseState) {
        return query(startIndex);
    }

    public Query queryForOrderMade(int startIndex, CaseState caseState,
                                   GeneralApplicationTypes gaType) {
        return query(startIndex);
    }

    public Query queryForBusinessProcessStatus(int startIndex, BusinessProcessStatus processStatus) {
        return query(startIndex);
    }


    protected int calculatePages(SearchResult searchResult) {
        log.info("Initially search service found {} case(s) ", searchResult.getTotal());
        return new BigDecimal(searchResult.getTotal()).divide(new BigDecimal(ES_DEFAULT_SEARCH_LIMIT), UP).intValue();
    }

    public Set<CaseDetails> getGeneralApplications(CaseState caseState) {
        SearchResult searchResult = coreCaseDataService.searchGeneralApplication(query(START_INDEX, caseState));
        int pages = calculatePages(searchResult);
        Set<CaseDetails> caseDetails = new HashSet<>(searchResult.getCases());

        for (int i = 1; i < pages; i++) {
            SearchResult result = coreCaseDataService
                    .searchGeneralApplication(query(i * ES_DEFAULT_SEARCH_LIMIT, caseState));
            caseDetails.addAll(result.getCases());
        }

        List<Long> ids = caseDetails.stream().map(CaseDetails::getId).sorted().toList();
        log.info("Found {} case(s) with ids {}", ids.size(), ids);

        return caseDetails;
    }

    public Set<CaseDetails> getOrderMadeGeneralApplications(CaseState caseState, GeneralApplicationTypes gaType) {

        SearchResult searchResult = coreCaseDataService
                .searchGeneralApplication(queryForOrderMade(START_INDEX, caseState, gaType));

        int pages = calculatePages(searchResult);
        Set<CaseDetails> caseDetails = new HashSet<>(searchResult.getCases());

        for (int i = 1; i < pages; i++) {
            SearchResult result = coreCaseDataService
                    .searchGeneralApplication(queryForOrderMade(i * ES_DEFAULT_SEARCH_LIMIT, caseState, gaType));
            caseDetails.addAll(result.getCases());
        }

        List<Long> ids = caseDetails.stream().map(CaseDetails::getId).sorted().toList();
        log.info("Found {} case(s) with ids {}", ids.size(), ids);

        return caseDetails;
    }
}
