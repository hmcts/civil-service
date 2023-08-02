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
public class CaseSdtRequestIdSearchService {

    private final CoreCaseDataService coreCaseDataService;

    public boolean getCaseDataBySdtRequest(String sdtRequestId) {
        Query query = new Query(boolQuery().must(
            matchQuery("data.sdtRequestId", sdtRequestId)), List.of(), 0);
        SearchResult searchResult = coreCaseDataService.searchCases(query);
        if (searchResult == null || searchResult.getCases().size() < 1) {
            return true;
        }
        return false;
    }
}
