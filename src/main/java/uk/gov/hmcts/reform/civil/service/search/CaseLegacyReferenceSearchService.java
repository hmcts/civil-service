package uk.gov.hmcts.reform.civil.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.search.exceptions.SearchServiceCaseNotFoundException;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseLegacyReferenceSearchService {

    private final CoreCaseDataService coreCaseDataService;

    public CaseDetails getCaseDataByLegacyReference(String legacyReference) {
        Query query = new Query(boolQuery().must(
            matchQuery("data.legacyCaseReference", legacyReference)), List.of(), 0);
        SearchResult searchResult = coreCaseDataService.searchCases(query);
        if (searchResult == null || searchResult.getCases().isEmpty()) {
            log.error("no case found for {}", legacyReference);
            throw new SearchServiceCaseNotFoundException();
        }
        return searchResult.getCases().get(0);
    }

    public CaseDetails getCivilOrOcmcCaseDataByCaseReference(String caseReference) {
        log.info("searching cases with reference {}", caseReference);
        Query query = new Query(boolQuery().must(
            boolQuery()
                .should(matchQuery("data.legacyCaseReference", caseReference))
                .should(matchQuery("data.previousServiceCaseReference", caseReference))
                .minimumShouldMatch(1)
        ), List.of(), 0);
        SearchResult searchResult = coreCaseDataService.searchCases(query);
        if (searchResult == null || searchResult.getCases().isEmpty()) {
            searchResult = coreCaseDataService.searchCMCCases(query);
            if (searchResult == null || searchResult.getCases().isEmpty()) {
                log.error("no case found for {}", caseReference);
                throw new SearchServiceCaseNotFoundException();
            }
        }
        return searchResult.getCases().get(0);
    }
}
