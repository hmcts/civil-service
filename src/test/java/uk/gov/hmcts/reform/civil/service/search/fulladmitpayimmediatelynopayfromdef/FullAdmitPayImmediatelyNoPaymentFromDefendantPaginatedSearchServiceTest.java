package uk.gov.hmcts.reform.civil.service.search.fulladmitpayimmediatelynopayfromdef;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchPaginatedStreamProvider;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FullAdmitPayImmediatelyNoPaymentFromDefendantPaginatedSearchServiceTest {

    @Mock
    private ElasticSearchPaginatedStreamProvider elasticSearchPaginatedStreamProvider;
    @Mock
    private FullAdmitPayImmediatelyNoPaymentFromDefendantQueryProvider queryProvider;
    @Mock
    private ElasticSearchResult elasticSearchResult;

    private FullAdmitPayImmediatelyNoPaymentFromDefendantPaginatedSearchService searchService;

    @BeforeEach
    void setup() {
        searchService = new FullAdmitPayImmediatelyNoPaymentFromDefendantPaginatedSearchService(
            elasticSearchPaginatedStreamProvider,
            queryProvider
        );
        ReflectionTestUtils.setField(searchService, "pageSize", 50);
    }

    @Test
    void shouldCallStreamProviderWithCorrectParameters() {
        when(elasticSearchPaginatedStreamProvider.getPaginatedSearchResult(queryProvider, 50))
            .thenReturn(elasticSearchResult);

        ElasticSearchResult result = searchService.getElasticSearchResult();

        assertThat(result).isEqualTo(elasticSearchResult);
        verify(elasticSearchPaginatedStreamProvider).getPaginatedSearchResult(queryProvider, 50);
    }
}
