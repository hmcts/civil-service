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
        if (carmEnabled) {
            SearchResult searchResult = coreCaseDataService.searchMediationCases(queryInMediationCases(START_INDEX, claimMovedDate,
                                                                                                       carmEnabled, true, null));
            log.info("mediation total found: {}", searchResult.getTotal());
            int pages = calculatePages(searchResult);
            List<CaseDetails> caseDetails = new ArrayList<>(searchResult.getCases());
            if (!caseDetails.isEmpty()) {
                StringBuilder sb = new StringBuilder().append("Mediation query case IDs: ");
                for (CaseDetails caseDetail : caseDetails) {
                    sb.append(caseDetail.getId());
                    sb.append("\n");
                }
                log.info(sb.toString());
            }
            for (int i = 1; i < pages; i++) {
                String searchAfterValue = searchResult.getCases().get(searchResult.getCases().size() - 1).getId().toString();
                SearchResult result = coreCaseDataService.searchMediationCases(queryInMediationCases(i * ES_DEFAULT_SEARCH_LIMIT, claimMovedDate,
                                                                                            carmEnabled, false, searchAfterValue));
                if (!result.getCases().isEmpty()) {
                    StringBuilder sb = new StringBuilder().append("Page ").append(i).append(" Mediation query case IDs: ");
                    for (CaseDetails caseDetail : result.getCases()) {
                        sb.append(caseDetail.getId());
                        sb.append("\n");
                    }
                    log.info(sb.toString());
                }
                caseDetails.addAll(result.getCases());
            }

            return caseDetails;
        } else {
            SearchResult searchResult = coreCaseDataService.searchCases(queryInMediationCases(START_INDEX, claimMovedDate,
                                                                                              carmEnabled, false, null));
            log.info("mediation total found: {}", searchResult.getTotal());
            int pages = calculatePages(searchResult);
            List<CaseDetails> caseDetails = new ArrayList<>(searchResult.getCases());
            if (!caseDetails.isEmpty()) {
                StringBuilder sb = new StringBuilder().append("Mediation query case IDs: ");
                for (CaseDetails caseDetail : caseDetails) {
                    sb.append(caseDetail.getId());
                    sb.append("\n");
                }
                log.info(sb.toString());
            }
            for (int i = 1; i < pages; i++) {
                SearchResult result = coreCaseDataService.searchCases(queryInMediationCases(i * ES_DEFAULT_SEARCH_LIMIT, claimMovedDate,
                                                                                            carmEnabled, false, null));
                if (!result.getCases().isEmpty()) {
                    StringBuilder sb = new StringBuilder().append("Page ").append(i).append(" Mediation query case IDs: ");
                    for (CaseDetails caseDetail : result.getCases()) {
                        sb.append(caseDetail.getId());
                        sb.append("\n");
                    }
                    log.info(sb.toString());
                }
                caseDetails.addAll(result.getCases());
            }

            return caseDetails;
        }
    }

    abstract Query query(int startIndex);

    abstract Query queryInMediationCases(int startIndex, LocalDate claimMovedDate, boolean carmEnabled, boolean initialSearch,
                                         String searchAfterValue);

    private int calculatePages(SearchResult searchResult) {
        return new BigDecimal(searchResult.getTotal()).divide(new BigDecimal(ES_DEFAULT_SEARCH_LIMIT), UP).intValue();
    }
}
