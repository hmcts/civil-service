package uk.gov.hmcts.reform.civil.service.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.search.exceptions.CaseNotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class CaseLegacyReferenceSearchServiceTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @InjectMocks
    private CaseLegacyReferenceSearchService caseLegacyReferenceSearchService;

    private SearchResult searchResult;

    private final String REFERENCE = "ABC";
    private final Query EXPECTED_QUERY = new Query(boolQuery().must(matchQuery("data.legacyCaseReference", REFERENCE)), List.of(), 0);

    @BeforeEach
    private void setUp(){
        searchResult = mock(SearchResult.class);
        given(coreCaseDataService.searchCases(any())).willReturn(searchResult);
    }
    @Test
    void shouldReturnCaseDataSuccessfully_whenCaseExits(){
        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        CaseData caseData = CaseData.builder().build();
        given(searchResult.getCases()).willReturn(Arrays.asList(caseDetails));
        given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseData);

       CaseData result = caseLegacyReferenceSearchService.getCaseDataByLegacyReference(REFERENCE);

       assertThat(result).isNotNull();
       verify(coreCaseDataService).searchCases(refEq(EXPECTED_QUERY));
       verify(caseDetailsConverter).toCaseData(caseDetails);
    }

    @Test
    void shouldThrowException_whenCaseIsNotFound(){
        given(searchResult.getCases()).willReturn(Collections.emptyList());

        assertThrows(CaseNotFoundException.class,
                     () -> caseLegacyReferenceSearchService.getCaseDataByLegacyReference(REFERENCE));
    }

}
