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
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.search.exceptions.SearchServiceCaseNotFoundException;

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

    private static final String REFERENCE = "ABC";
    private static final Query EXPECTED_CIVIL_QUERY =
        new Query(boolQuery().must(matchQuery("data.legacyCaseReference", REFERENCE)), List.of(), 0);
    private static final Query EXPECTED_OCMC_QUERY =
        new Query(boolQuery().must(matchQuery("data.previousServiceCaseReference", REFERENCE)), List.of(), 0);

    @BeforeEach
    private void setUp() {
        searchResult = mock(SearchResult.class);
        given(coreCaseDataService.searchCases(any())).willReturn(searchResult);
    }

    @Test
    void shouldReturnCaseDetailsSuccessfully_whenCaseExits() {
        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        given(searchResult.getCases()).willReturn(Arrays.asList(caseDetails));

        CaseDetails result = caseLegacyReferenceSearchService.getCaseDataByLegacyReference(REFERENCE);

        assertThat(result).isNotNull();
        verify(coreCaseDataService).searchCases(refEq(EXPECTED_CIVIL_QUERY));
    }

    @Test
    void shouldThrowException_whenCaseIsNotFound() {
        given(searchResult.getCases()).willReturn(Collections.emptyList());

        assertThrows(
            SearchServiceCaseNotFoundException.class, () ->
                caseLegacyReferenceSearchService.getCaseDataByLegacyReference(REFERENCE));
    }

    @Test
    void shouldReturnCaseDetailsSuccessfully_getCivilOrOcmcCaseDataByCaseReference_whenCaseExits() {
        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        given(searchResult.getCases()).willReturn(Arrays.asList(caseDetails));

        CaseDetails result = caseLegacyReferenceSearchService.getCivilOrOcmcCaseDataByCaseReference(REFERENCE);

        assertThat(result).isNotNull();
        verify(coreCaseDataService).searchCases(refEq(EXPECTED_CIVIL_QUERY));
    }

    @Test
    void shouldReturnCaseDetailsSuccessfullyFromOCMC_getCivilOrOcmcCaseDataByCaseReference_whenCaseExits() {
        given(coreCaseDataService.searchCases(any())).willReturn(null);
        given(coreCaseDataService.searchCMCCases(any())).willReturn(searchResult);

        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        given(searchResult.getCases()).willReturn(Arrays.asList(caseDetails));

        CaseDetails result = caseLegacyReferenceSearchService.getCivilOrOcmcCaseDataByCaseReference(REFERENCE);

        assertThat(result).isNotNull();
        verify(coreCaseDataService).searchCases(refEq(EXPECTED_CIVIL_QUERY));
        verify(coreCaseDataService).searchCMCCases(refEq(EXPECTED_OCMC_QUERY));
    }

    @Test
    void shouldReturnEmpty_getCivilOrOcmcCaseDataByCaseReference_whenCaseIsNotFound() {
        given(searchResult.getCases()).willReturn(Collections.emptyList());

        CaseDetails result = caseLegacyReferenceSearchService.getCivilOrOcmcCaseDataByCaseReference(REFERENCE);
        assertThat(result).isNull();
    }

}
