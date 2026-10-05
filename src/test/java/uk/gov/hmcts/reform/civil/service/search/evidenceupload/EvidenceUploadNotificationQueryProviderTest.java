package uk.gov.hmcts.reform.civil.service.search.evidenceupload;

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
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.search.common.CommonQueryConstructs;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvidenceUploadNotificationQueryProviderTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 15, 10, 0);

    private final ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @Spy
    private CommonQueryConstructs commonQueryConstructs;

    @Mock
    private Time time;

    @InjectMocks
    private EvidenceUploadNotificationQueryProvider provider;

    @BeforeEach
    void setUp() {
        when(time.now()).thenReturn(NOW);
    }

    @Test
    void shouldReturnCorrectInitialPaginatedQuery() throws Exception {
        // Given
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

        assertThat(json.toString()).contains("PREPARE_FOR_HEARING_CONDUCT_HEARING");
        assertThat(json.toString()).contains("HEARING_READINESS");
        assertThat(json.toString()).contains("DECISION_OUTCOME");
        assertThat(json.toString()).contains("All_FINAL_ORDERS_ISSUED");
        assertThat(json.toString()).contains("CASE_PROGRESSION");
        assertThat(json.toString()).contains("data.evidenceUploadNotificationSent");
        assertThat(json.toString()).contains("data.caseDocumentUploadDate");
        assertThat(json.toString()).contains("data.caseDocumentUploadDateRes");
    }

    @Test
    void shouldSearchOverASevenDayUploadWindowEndingNow() throws Exception {
        // Given
        PageToken pageToken = PageToken.initial();

        // When
        PaginatedQuery query = provider.getPaginatedQuery(pageToken, 50);

        // Then
        JsonNode json = objectMapper.readTree(query.getJsonString(objectMapper));
        JsonNode dateRanges = json.at("/query/bool/must/1/bool/should");

        assertThat(dateRanges.get(0).at("/range/data.caseDocumentUploadDate/gt").asText())
            .isEqualTo("2026-01-08T10:00:00.000Z");
        assertThat(dateRanges.get(0).at("/range/data.caseDocumentUploadDate/lt").asText())
            .isEqualTo("2026-01-15T10:00:00.000Z");
        assertThat(dateRanges.get(1).at("/range/data.caseDocumentUploadDateRes/gt").asText())
            .isEqualTo("2026-01-08T10:00:00.000Z");
        assertThat(dateRanges.get(1).at("/range/data.caseDocumentUploadDateRes/lt").asText())
            .isEqualTo("2026-01-15T10:00:00.000Z");
    }

    @Test
    void shouldReturnCorrectQueryWithSearchAfter() throws Exception {
        // Given
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
