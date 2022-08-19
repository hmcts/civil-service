package uk.gov.hmcts.reform.civil.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.search.exceptions.CaseNotFoundException;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseLegacyReferenceSearchService {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    public CaseData getCaseDataByLegacyReference(String legacyReference) {
        Query query = new Query(boolQuery().must(
            matchQuery("data.legacyCaseReference", legacyReference)), List.of(), 0);
        SearchResult searchResult = coreCaseDataService.searchCases(query);
        if (searchResult == null || searchResult.getCases().size() < 1) {
            log.error("no case found for {}", legacyReference);
            throw new CaseNotFoundException();
        }
        return caseDetailsConverter.toCaseData(searchResult.getCases().get(0));
    }

}
