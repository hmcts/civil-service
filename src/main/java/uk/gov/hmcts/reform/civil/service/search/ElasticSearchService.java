package uk.gov.hmcts.reform.civil.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.math.RoundingMode.UP;

@RequiredArgsConstructor
@Slf4j
public abstract class ElasticSearchService {

    private final CoreCaseDataService coreCaseDataService;
    private static final int START_INDEX = 0;
    private static final int ES_DEFAULT_SEARCH_LIMIT = 10;

    public List<CaseDetails> getCases() {
        SearchResult searchResult = coreCaseDataService.searchCases(query(START_INDEX));
        int pages = calculatePages(searchResult);
        List<CaseDetails> caseDetails = new ArrayList<>(searchResult.getCases());

        for (int i = 1; i < pages; i++) {
            SearchResult result = coreCaseDataService.searchCases(query(i * ES_DEFAULT_SEARCH_LIMIT));
            caseDetails.addAll(result.getCases());
        }

        return caseDetails;
    }

    public List<CaseDetails> getInMediationCases(LocalDate claimMovedDate, boolean carmEnabled) {

        if (claimMovedDate == null) {
            claimMovedDate = LocalDate.now().minusDays(1);
        }
        SearchResult searchResult = coreCaseDataService.searchCases(queryInMediationCases(START_INDEX, claimMovedDate, carmEnabled));
        int pages = calculatePages(searchResult);
        List<CaseDetails> caseDetails = new ArrayList<>(searchResult.getCases());

        for (int i = 1; i < pages; i++) {
            SearchResult result = coreCaseDataService.searchCases(queryInMediationCases(i * ES_DEFAULT_SEARCH_LIMIT, claimMovedDate, carmEnabled));
            caseDetails.addAll(result.getCases());
        }

        return caseDetails;
    }

    abstract Query query(int startIndex);

    abstract Query queryInMediationCases(int startIndex, LocalDate claimMovedDate, boolean carmEnabled);

    private int calculatePages(SearchResult searchResult) {
        return new BigDecimal(searchResult.getTotal()).divide(new BigDecimal(ES_DEFAULT_SEARCH_LIMIT), UP).intValue();
    }
}
