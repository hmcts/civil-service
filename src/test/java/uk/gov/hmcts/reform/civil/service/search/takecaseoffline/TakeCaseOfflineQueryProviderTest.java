package uk.gov.hmcts.reform.civil.service.search.takecaseoffline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.search.PageToken;
import uk.gov.hmcts.reform.civil.model.search.PaginatedQuery;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.search.common.CommonQueryConstructs;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TakeCaseOfflineQueryProviderTest {

    private FeatureToggleService featureToggleService;
    private CommonQueryConstructs commonQueryConstructs;
    private Time time;
    private TakeCaseOfflineQueryProvider queryProvider;

    @BeforeEach
    void setUp() {
        featureToggleService = mock(FeatureToggleService.class);
        commonQueryConstructs = new CommonQueryConstructs();
        time = mock(Time.class);
        queryProvider = new TakeCaseOfflineQueryProvider(featureToggleService, commonQueryConstructs, time);
    }

    @Test
    void shouldReturnPaginatedQuery_whenCalled() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 0, 0);
        when(time.now()).thenReturn(now);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);

        PaginatedQuery query = queryProvider.getPaginatedQuery(PageToken.initial(), 50);

        assertThat(query).isNotNull();
        assertThat(query.getPageSize()).isEqualTo(50);
        assertThat(query.getDataToReturn()).containsExactly("reference");

        String queryStr = query.getQueryBuilder().toString();
        assertThat(queryStr).contains("applicant1ResponseDeadline");
        assertThat(queryStr).contains("addLegalRepDeadlineRes1");
        assertThat(queryStr).contains("addLegalRepDeadlineRes2");
        assertThat(queryStr).doesNotContain("data.businessProcess.status");
    }

    @Test
    void shouldReturnPaginatedQueryWithWelshEnabled_whenCalled() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 0, 0);
        when(time.now()).thenReturn(now);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);

        PaginatedQuery query = queryProvider.getPaginatedQuery(PageToken.initial(), 50);

        assertThat(query).isNotNull();
        String queryStr = query.getQueryBuilder().toString();
        assertThat(queryStr).contains("applicant1ResponseDate");
    }
}
