package uk.gov.hmcts.reform.civil.service.search.defendantresponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.search.PageToken;
import uk.gov.hmcts.reform.civil.model.search.PaginatedQuery;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.search.common.CommonQueryConstructs;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefendantResponseDeadlineCheckQueryProviderTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 15, 10, 0);

    private final ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @Spy
    private CommonQueryConstructs commonQueryConstructs;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private Time time;

    @InjectMocks
    private DefendantResponseDeadlineCheckQueryProvider provider;

    @BeforeEach
    void setUp() {
        when(time.now()).thenReturn(NOW);
    }

    @Test
    void shouldReturnCorrectInitialPaginatedQuery() throws Exception {
        // Given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        PageToken pageToken = PageToken.initial();
        int pageSize = 50;

        // When
        PaginatedQuery query = provider.getPaginatedQuery(pageToken, pageSize);

        // Then
        JsonNode json = objectMapper.readTree(query.getJsonString(objectMapper));

        assertThat(json.get("size").asInt()).isEqualTo(50);
        assertThat(json.get("from").asInt()).isZero();
        assertThat(json.get("track_total_hits").asBoolean()).isTrue();
        assertThat(json.has("search_after")).isFalse();

        assertThat(json.get("_source").get(0).asText()).isEqualTo("reference");
        assertThat(json.get("sort").get(0).get("reference.keyword").asText()).isEqualTo("asc");

        assertThat(json.toString()).contains("data.respondent1ResponseDeadline");
        assertThat(json.toString()).contains("2026-01-15T10:00Z");
        assertThat(json.toString()).contains("data.respondent1ResponseDeadlineChecked");
        assertThat(json.toString()).contains("AWAITING_RESPONDENT_ACKNOWLEDGEMENT");
        assertThat(json.toString()).contains("data.businessProcess");
        assertThat(json.toString()).doesNotContain("data.respondent1ResponseDate");
    }

    @Test
    void shouldExcludeCasesWithAResponseDate_whenWelshIsEnabledForMainCase() throws Exception {
        // Given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);

        // When
        PaginatedQuery query = provider.getPaginatedQuery(PageToken.initial(), 50);

        // Then
        JsonNode json = objectMapper.readTree(query.getJsonString(objectMapper));

        assertThat(json.toString()).contains("data.respondent1ResponseDate");
        assertThat(json.toString()).contains("AWAITING_RESPONDENT_ACKNOWLEDGEMENT");
    }

    @Test
    void shouldReturnCorrectQueryWithSearchAfter() throws Exception {
        // Given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        PageToken pageToken = PageToken.of("12345");
        int pageSize = 10;

        // When
        PaginatedQuery query = provider.getPaginatedQuery(pageToken, pageSize);

        // Then
        JsonNode json = objectMapper.readTree(query.getJsonString(objectMapper));

        assertThat(json.get("size").asInt()).isEqualTo(10);
        assertThat(json.get("from").asInt()).isZero();
        assertThat(json.get("search_after").get(0).asText()).isEqualTo("12345");
        assertThat(json.get("sort").get(0).get("reference.keyword").asText()).isEqualTo("asc");
    }
}
