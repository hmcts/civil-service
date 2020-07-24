package uk.gov.hmcts.reform.unspec.service;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

@ExtendWith(SpringExtension.class)
class CaseSearchServiceTest {

    public static final SearchResult EXPECTED_SEARCH_RESULTS = SearchResult.builder()
        .total(1)
        .cases(List.of(CaseDetails.builder().id(1L).build()))
        .build();

    @Captor
    private ArgumentCaptor<String> queryCaptor;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private CaseSearchService searchService;

    @BeforeEach
    void setup() {
        when(coreCaseDataService.searchCases(any())).thenReturn(EXPECTED_SEARCH_RESULTS);
    }

    @Test
    void shouldGetCases_WhenSearchingCasesByDateProperty() throws JSONException {
        String expectedQuery = "{\"query\":"
            + "{\"range\":{\"data.claimIssuedDate\":{\"lt\":\"now-112d\"}}}, "
            + "\"_source\": [\"reference\"]}";

        assertThat(searchService.getCasesToBeStayed()).isEqualTo(EXPECTED_SEARCH_RESULTS.getCases());
        verify(coreCaseDataService).searchCases(queryCaptor.capture());
        JSONAssert.assertEquals(queryCaptor.getValue(), expectedQuery, STRICT);
    }
}
